using System.Collections.Generic;
using Newtonsoft.Json.Linq;

namespace TcpServer
{
    // To represent a JSON response
    public class Response
    {
        public int Status { get; set; }
        public Dictionary<string, string> Headers { get; set; }
        public JObject Payload { get; set; }
    }
}