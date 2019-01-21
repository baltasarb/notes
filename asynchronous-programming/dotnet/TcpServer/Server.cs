using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using Newtonsoft.Json.Linq;

namespace TcpServer
{
    public class Server
    {
        private readonly object _shutdownMonitor;

        //the server message storage
        private readonly ConcurrentDictionary<string, AsyncQueue<JObject>> _queues;

        //string : method to execute
        //delegate: the method's corresponding function
        private Dictionary<string, Delegate> _methodResolver;

        //the cancellation mechanism used to shutdown the server
        private readonly CancellationTokenSource _cancellationTokenSource;

        //the server's state, online or offline
        private volatile ServerState _serverState;

        //used to notify the waiting shutdown task that all tasks are finished
        private readonly CancellationTokenSource _shutdownCompletionSource;

        private enum ServerState
        {
            Online,
            Offline
        };

        public Server(CancellationTokenSource cancellationTokenSource)
        {
            _shutdownMonitor = new object();
            _cancellationTokenSource = cancellationTokenSource;
            _queues = new ConcurrentDictionary<string, AsyncQueue<JObject>>();
            _serverState = ServerState.Online;
            _shutdownCompletionSource = new CancellationTokenSource();
            InitializeMethodResolver();
        }

        private void InitializeMethodResolver()
        {
            _methodResolver = new Dictionary<string, Delegate>
            {
                {"create", new Func<Request, Task<Response>>(Create)},
                {"send", new Func<Request, Task<Response>>(Send)},
                {"receive", new Func<Request, Task<Response>>(Receive)},
                {"shutdown", new Func<Request, Task<Response>>(Shutdown)}
            };
        }

        //method responsible for executing the function requested by the client
        //it chooses one of the available functions inside the _methodResolver structure
        public async Task<Response> ResolveRequest(Request request)
        {
            if (_serverState == ServerState.Offline)
            {
                return ResponseFactory.ServiceUnavailableResponse();
            }

            bool methodExists = _methodResolver.TryGetValue(request.Method.ToLower(), out var serverOperation);

            if (!methodExists)
            {
                return ResponseFactory.InvalidRequestResponse("The provided method is not supported.");
            }

            return await (Task<Response>) serverOperation.DynamicInvoke(request);
        }

        private Task<Response> Create(Request request)
        {
            bool added = _queues.TryAdd(request.Path, new AsyncQueue<JObject>());

            return added
                ? Task.FromResult(ResponseFactory.SuccessResponse())
                : Task.FromResult(ResponseFactory.InvalidRequestResponse("The given path already exists."));
        }

        private Task<Response> Send(Request request)
        {
            bool pathExists = _queues.TryGetValue(request.Path, out var queue);

            Response response;

            if (!pathExists)
            {
                response = ResponseFactory.QueueDoesNotExistResponse();
            }

            else
            {
                queue.Enqueue(request.Payload);
                response = ResponseFactory.SuccessResponse("Message sent with success.");
            }

            return Task.FromResult(response);
        }

        private async Task<Response> Receive(Request request)
        {
            bool pathExists = _queues.TryGetValue(request.Path, out var queue);

            if (!pathExists)
            {
                return ResponseFactory.InvalidRequestResponse("The given path does not exist.");
            }

            int timeout;
            try
            {
                bool timeoutIsNumber = int.TryParse(request.Headers["timeout"], out timeout);

                if (!timeoutIsNumber) return ResponseFactory.InvalidRequestResponse("Invalid timeout format.");

                if (timeout <= 0)
                {
                    return ResponseFactory.TimeoutResponse();
                }
            }
            catch (Exception)
            {
                return ResponseFactory.InvalidRequestResponse("Invalid timeout format.");
            }

            JObject payload;
            try
            {
                payload = await queue.Dequeue(TimeSpan.FromMilliseconds(timeout), _cancellationTokenSource);
            }
            catch (TimeoutException)
            {
                return ResponseFactory.TimeoutResponse();
            }

            return ResponseFactory.SuccessResponse(payload);
        }

        public async Task<Response> Shutdown(Request request)
        {
            if (_serverState == ServerState.Online)
            {
                lock (_shutdownMonitor)
                {
                    if (_serverState == ServerState.Online)
                    {
                        _serverState = ServerState.Offline;
                    }
                }
            }

            int timeout;
            try
            {
                bool timeoutIsNumber = int.TryParse(request.Headers["timeout"], out timeout);

                if (!timeoutIsNumber) return ResponseFactory.InvalidRequestResponse("Invalid timeout format.");

                if (timeout <= 0)
                {
                    return ResponseFactory.TimeoutResponse();
                }
            }
            catch (Exception)
            {
                return ResponseFactory.InvalidRequestResponse("Invalid timeout format.");
            }

            _cancellationTokenSource.Cancel();

            Response response;
            try
            {
                await Task.Delay(timeout, _shutdownCompletionSource.Token);
                response = ResponseFactory.TimeoutResponse("The server is shutting down, waiting time expired.");
            }
            catch (OperationCanceledException)
            {
                response = ResponseFactory.SuccessResponse("Server shutdown successful.");
            }

            return response;
        }

        public void NotifyServerShutdownIsComplete()
        {
            _shutdownCompletionSource.Cancel();
        }
    }
}