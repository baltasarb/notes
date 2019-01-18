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
        private const int MaximumConcurrentClientsAllowed = 10;
        private readonly ConcurrentDictionary<string, AsyncQueue<JObject>> _queues;

        private Dictionary<string, Delegate> _methodResolver;

        private readonly object _shutdownMonitor;
        private volatile ServerState _serverState;

        private volatile int _activeClients;

        private enum ServerState
        {
            Online,
            ShuttingDown,
            Offline
        };

        public Server()
        {
            _shutdownMonitor = new object();
            _queues = new ConcurrentDictionary<string, AsyncQueue<JObject>>();
            _serverState = ServerState.Online;
            _activeClients = 0;
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

        public async Task<Response> ResolveRequest(Request request)
        {
            if (_serverState == ServerState.ShuttingDown || _serverState == ServerState.Offline ||
                _activeClients == MaximumConcurrentClientsAllowed)
            {
                return ResponseFactory.ServiceUnavailableResponse();
            }

            bool methodExists = _methodResolver.TryGetValue(request.Method.ToLower(), out var operation);

            if (!methodExists)
            {
                return ResponseFactory.InvalidRequestResponse();
            }

            Interlocked.Increment(ref _activeClients);

            return await (Task<Response>) operation.DynamicInvoke(request);
        }

        private Task<Response> Create(Request request)
        {
            bool added = _queues.TryAdd(request.Path, new AsyncQueue<JObject>());
            Interlocked.Decrement(ref _activeClients);
            return added
                ? Task.FromResult(ResponseFactory.SuccessResponse())
                : Task.FromResult(ResponseFactory.InvalidRequestResponse());
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
                response = ResponseFactory.SuccessResponse();
            }

            Interlocked.Decrement(ref _activeClients);

            return Task.FromResult(response);
        }

        private async Task<Response> Receive(Request request)
        {
            bool pathExists = _queues.TryGetValue(request.Path, out var queue);

            if (!pathExists)
            {
                return ResponseFactory.InvalidRequestResponse();
            }

            //todo exception in parsing
            bool isNumber = int.TryParse(request.Headers["timeout"], out int timeout);

            if (!isNumber) return ResponseFactory.InvalidRequestResponse();

            JObject payload;
            try
            {
                payload = await queue.Dequeue(TimeSpan.FromMilliseconds(timeout), new CancellationTokenSource());
            }
            catch (TimeoutException)
            {
                return ResponseFactory.TimeoutResponse();
            }

            return ResponseFactory.SuccessResponse(payload);
        }

        public Task<Response> Shutdown(Request request)
        {

            throw new System.NotImplementedException();
        }
    }
}