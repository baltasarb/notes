using System.Collections.Generic;
using Newtonsoft.Json.Linq;

namespace TcpServer
{
    public class Server : IServerOperations
    {
        private Dictionary<string, List<JObject>> _queues;

        public Server()
        {
            _queues = new Dictionary<string, List<JObject>>();
        }

        public void Create(string path)
        {
            throw new System.NotImplementedException();
        }

        public void Send(string path, JObject payload)
        {
            throw new System.NotImplementedException();
        }

        public void Receive(string path, out JObject payload, long timeout)
        {
            throw new System.NotImplementedException();
        }

        public void Shutdown(JObject payload)
        {
            throw new System.NotImplementedException();
        }
    }
}