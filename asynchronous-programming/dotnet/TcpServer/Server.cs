using System.Collections.Concurrent;
using System.Threading;
using Newtonsoft.Json.Linq;

namespace TcpServer
{
    public class Server : IServerOperations
    {
        private readonly int _initialCapacity = 101;
        private readonly ConcurrentDictionary<string, ConcurrentQueue<JObject>> _queues;

        private readonly object _shutdownMonitor;
        private volatile ServerState _serverState;

        private enum ServerState
        {
            Online,
            ShuttingDown,
            Offline
        };

        public Server(int maximumConcurrency)
        {
            _shutdownMonitor = new object();
            _queues = new ConcurrentDictionary<string, ConcurrentQueue<JObject>>(maximumConcurrency, _initialCapacity);
            _serverState = ServerState.Online;
        }

        public Response Create(Request request)
        {
            if (_serverState == ServerState.ShuttingDown)
            {
                return ResponseFactory.ServiceUnavailableResponse();
            }

            bool added = _queues.TryAdd(request.Path, new ConcurrentQueue<JObject>());

            return added ? ResponseFactory.SuccessResponse() : ResponseFactory.InvalidRequestResponse();
        }

        public Response Send(Request request)
        {
            if (_serverState == ServerState.ShuttingDown)
            {
                return ResponseFactory.ServiceUnavailableResponse();
            }

            bool pathExists = _queues.TryGetValue(request.Path, out var queue);

            if (!pathExists)
            {
                return ResponseFactory.QueueDoesNotExistResponse();
            }

            queue.Enqueue(request.Payload);
            return ResponseFactory.SuccessResponse();
        }

        public Response Receive(Request request, out JObject payload)
        {


            throw new System.NotImplementedException();
        }

        public Response Shutdown(Request request)
        {
            if (_serverState == ServerState.Offline)
            {
                //todo ?
                return ResponseFactory.SuccessResponse();
            }

            if (_serverState == ServerState.ShuttingDown)
            {
                //todo already shutting down what to do? multiple waiters?
                return ResponseFactory.ServiceUnavailableResponse();
            }

            lock (_shutdownMonitor)
            {
                try
                {
                    while (true)
                    {
                        _serverState = ServerState.ShuttingDown;

                        Monitor.Wait(_shutdownMonitor);

                        if (_serverState == ServerState.Offline)
                        {
                            //todo success ??
                            return ResponseFactory.SuccessResponse();
                        }
                    }
                }
                catch (ThreadInterruptedException)
                {
                    if (_serverState == ServerState.Offline)
                    {
                        //todo success ??
                        return ResponseFactory.SuccessResponse();
                    }

                    throw;
                }
            }
        }
    }
}