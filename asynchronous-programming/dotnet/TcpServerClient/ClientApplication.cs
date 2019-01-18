using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace TcpServerClient
{
    class ClientApplication
    {
        private const int Port = 8081;
        private static readonly JsonSerializer Serializer = new JsonSerializer();

        private static async Task Main()
        {
            using (TcpClient tcpClient = new TcpClient())
            {
                tcpClient.Connect(IPAddress.Loopback, Port);
                var stream = tcpClient.GetStream();

                try
                {
                    while (true)
                    {
                        ShowMenu();

                        var requestType = GetRequestType();
                        var request = BuildRequest(requestType);

                        await MakeRequest(stream, request);

                        var response = await ReadResponse(stream);

                        PrintResponse(response);
                    }
                }
                catch (Exception e)
                {
                    Console.WriteLine("Error : {0}.", e.Message);
                }
            }
        }

        private static void ShowMenu()
        {
            Console.WriteLine("Options:");
            Console.WriteLine("1. Create");
            Console.WriteLine("2. Send");
            Console.WriteLine("3. Receive");
            Console.WriteLine("4. Shutdown\n");
        }

        private static int GetRequestType()
        {
            return Int32.Parse(Console.ReadLine() ?? throw new InvalidOperationException());
        }

        private static Request BuildRequest(int requestType)
        {
            if (requestType == 1)
            {
                return RequestFactory.Create("path1");
            }

            if (requestType == 2)
            {
                return RequestFactory.Send("path1", JObject.Parse("{Message: 'message'}"));
            }

            if (requestType == 3)
            {
                return RequestFactory.Receive("path1", 10000);
            }

            if (requestType == 4)
            {
                return RequestFactory.Shutdown();
            }

            throw new InvalidOperationException();
        }

        private static async Task MakeRequest(Stream stream, Request request)
        {
            var writer = new JsonTextWriter(new StreamWriter(stream));
            Serializer.Serialize(writer, request);
            await writer.FlushAsync();
        }

        private static async Task<Response> ReadResponse(Stream stream)
        {
            var reader = new JsonTextReader(new StreamReader(stream))
            {
                // To support reading multiple top-level objects
                SupportMultipleContent = true
            };

            // to consume any bytes until start of object ('{')
            do
            {
                await reader.ReadAsync();
            } while (reader.TokenType != JsonToken.StartObject
                     && reader.TokenType != JsonToken.None);

            if (reader.TokenType == JsonToken.None)
            {
                Console.WriteLine($"[] reached end of input stream, ending.\n");
                throw new Exception();
            }

            var json = await JObject.LoadAsync(reader);
            // to ensure that proper deserialization is possible
            return json.ToObject<Response>();
        }

        private static void PrintResponse(Response response)
        {
            var stringBuilder = new StringBuilder();

            stringBuilder.Append("Response received:\n");

            stringBuilder.Append($"Status: {response.Status}.\n");

            if (response.Headers == null)
            {
                stringBuilder.Append($"Headers: no headers received.\n");
            }
            else
            {
                foreach (var header in response.Headers)
                {
                    stringBuilder.Append($"Header: {header.Key}, Value: {header.Value}.\n");
                }
            }


            if (response.Payload == null)
            {
                stringBuilder.Append($"Payload: no payload received.\n");
            }
            else
            {
                stringBuilder.Append($"Payload: {response.Payload}\n");
            }

            stringBuilder.Append($"Details: {response.Details}\n");

            Console.WriteLine(stringBuilder.ToString());
        }
    }
}