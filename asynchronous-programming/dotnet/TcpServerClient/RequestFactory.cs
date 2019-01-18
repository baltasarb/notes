using System.Collections.Generic;
using Newtonsoft.Json.Linq;

namespace TcpServerClient
{
    public class RequestFactory
    {
        public static Request Create(string path)
        {
            return new Request()
            {
                Path = path,
                Method = "Create",
                Headers = null,
                Payload = null
            };
        }

        public static Request Send(string path, JObject toSend)
        {
            return new Request()
            {
                Path = path,
                Headers = null,
                Method = "Send",
                Payload = toSend
            };
        }

        public static Request Receive(string path, int timeout)
        {
            return new Request()
            {
                Path = path,
                Headers = new Dictionary<string, string> {{"timeout", timeout.ToString()}},
                Method = "Receive",
                Payload = null
            };
        }

        public static Request Shutdown()
        {
            return null;
        }
    }
}