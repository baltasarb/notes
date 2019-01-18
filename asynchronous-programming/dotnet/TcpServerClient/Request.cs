using System.Collections.Generic;
using Newtonsoft.Json.Linq;

namespace TcpServerClient
{
    public class Request
    {
        public string Path; 
        public string Method;
        public Dictionary<string, string> Headers;
        public JObject Payload;

        public override string ToString()
        {
            return $"Method: {Method}, Headers: {Headers}, Payload: {Payload}";
        }
    }
}