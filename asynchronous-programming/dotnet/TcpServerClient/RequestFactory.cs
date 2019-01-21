using System.Collections.Generic;
using Newtonsoft.Json.Linq;

namespace TcpServerClient
{
    public class RequestFactory
    {
        private static int _sendMessageId;

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

        public static Request Send(string path)
        {
            return new Request()
            {
                Path = path,
                Headers = null,
                Method = "Send",
                Payload = JObject.Parse("{Message: 'message " + _sendMessageId++ + ".'}")
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

        public static Request Shutdown(int timeout)
        {
            return new Request()
            {
                Path = null,
                Headers = new Dictionary<string, string> { { "timeout", timeout.ToString() } },
                Method = "Shutdown",
                Payload = null
            };
        }
    }
}