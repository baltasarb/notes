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
    internal class ClientApplication
    {
        private const int Port = 8081;
        private static readonly JsonSerializer Serializer = new JsonSerializer();

        private static async Task Main()
        {
            using (var tcpClient = new TcpClient())
            {
                tcpClient.Connect(IPAddress.Loopback, Port);
                var stream = tcpClient.GetStream();

                try
                {
                    //check if the server is online before beginning (handshake)
                    if (!await EstablishConnection(stream))
                    {
                        return;
                    }
                }
                catch (Exception)
                {
                    Console.WriteLine("Server is not available.");
                    return;
                }

                while (true)
                {
                    try
                    {
                        ShowMenu();

                        var requestType = GetRequestType();

                        var request = BuildRequest(requestType);

                        await MakeRequest(stream, request);

                        var response = await ReadResponse(stream);

                        PrintResponse(response);

                        if (response.Details == "Server shutdown successful." || response.Status == 503) return;
                    }
                    catch (Exception e)
                    {
                        Console.WriteLine("Unknown error : {0}.", e.Message);
                        return;
                    }
                }
            }
        }

        private static async Task<bool> EstablishConnection(Stream stream)
        {
            Console.WriteLine("Waiting for the connection to the server to be established...");
            var connectionEstablishedResponse = await ReadResponse(stream);
            if (connectionEstablishedResponse.Status != 200)
            {
                Console.WriteLine("Server is offline and a connection could not be established.");
                return false;
            }

            Console.WriteLine("Connection established with success.");
            return true;
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
            string path;
            int timeout;

            switch (requestType)
            {
                case 1:
                    path = RequestAndGetPathFromInput();
                    return RequestFactory.Create(path);
                case 2:
                    path = RequestAndGetPathFromInput();
                    return RequestFactory.Send(path);
                case 3:
                    path = RequestAndGetPathFromInput();
                    timeout = RequestAndGetTimeoutFromInput();
                    return RequestFactory.Receive(path, timeout);
                case 4:
                    timeout = RequestAndGetTimeoutFromInput();
                    return RequestFactory.Shutdown(timeout);
                default:
                    throw new InvalidOperationException();
            }
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

            stringBuilder.Append("\nResponse received:\n");

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

        private static string RequestAndGetPathFromInput()
        {
            Console.Write("Path? ");
            var path = Console.ReadLine();
            return path;
        }

        private static int RequestAndGetTimeoutFromInput()
        {
            Console.Write("Timeout? ");
            var timeout = Console.ReadLine();
            return int.Parse(timeout ?? throw new InvalidOperationException());
        }
    }
}