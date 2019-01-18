using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace TcpServer
{
    public class ServerApplication
    {
        private const int Port = 8081;
        private static readonly Server Server = new Server();
        private static readonly JsonSerializer Serializer = new JsonSerializer();

        private static int _connectionIdCounter;

        private static async Task Main()
        {
            var listener = new TcpListener(IPAddress.Loopback, Port);
            listener.Start();

            Console.WriteLine($"Listening on {Port}");

            while (true)
            {
                var client = await listener.AcceptTcpClientAsync();

                Console.WriteLine($"connection accepted with id: {_connectionIdCounter++}.\n");

                try
                {
                    HandleResponse(client);
                }
                catch (Exception e)
                {
                    Console.WriteLine(e.Message);
                }
            }
        }

        public static async void HandleResponse(TcpClient client)
        {
            using (client)
            {
                var stream = client.GetStream();
                using (stream)
                {
                    while (true)
                    {
                        //serialize received request
                        var request = await ReadRequest(stream);

                        PrintRequest(request);

                        //send the serialized request to the server
                        var response = await Server.ResolveRequest(request);

                        //deserialize and send the response to the client
                        await WriteResponse(stream, response);
                    }
                }
            }
        }

        private static async Task<Request> ReadRequest(Stream stream)
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

            //if reader has not been called
            if (reader.TokenType == JsonToken.None)
            {
                Console.WriteLine($"[{_connectionIdCounter}] reached end of input stream, ending.");
                throw new Exception();
            }

            var json = await JObject.LoadAsync(reader);
            return json.ToObject<Request>();
            // to ensure that proper deserialization is possible
        }

        private static async Task WriteResponse(Stream stream, Response response)
        {
            var writer = new JsonTextWriter(new StreamWriter(stream));
            Serializer.Serialize(writer, response);
            await writer.FlushAsync();
        }

        private static void PrintRequest(Request request)
        {
            var stringBuilder = new StringBuilder();

            stringBuilder.Append("Request received:\n");

            stringBuilder.Append($"Path: {request.Path ?? "no path provided."}\n");
            stringBuilder.Append($"Method: {request.Method ?? "no method provided."}\n");

            if (request.Headers == null)
            {
                stringBuilder.Append("Header: no headers received.\n");
            }
            else
            {
                foreach (var header in request.Headers)
                {
                    stringBuilder.Append($"Header: {header.Key}, Value: {header.Value}.\n");
                }
            }

            if (request.Payload == null)
            {
                stringBuilder.Append("Payload: no payload received.\n");
            }
            else
            {
                stringBuilder.Append($"Payload: {request.Payload}\n");
            }

            Console.WriteLine($"Thread id: {Thread.CurrentThread.ManagedThreadId}");
            Console.WriteLine(stringBuilder.ToString());
        }
    }
}