using System.Collections.Generic;
using Newtonsoft.Json.Linq;

namespace TcpServer
{
    //To represent a json request
    public class Request
    {
        public string Method { get; set; }
        public string Path { get; set; }
        public Dictionary<string, string> Headers { get; set; }
        public JObject Payload { get; set; }

        public override string ToString()
        {
            return $"Method: {Method}, Path: {Path},Headers: {Headers}, Payload: {Payload}";
        }
    }
}