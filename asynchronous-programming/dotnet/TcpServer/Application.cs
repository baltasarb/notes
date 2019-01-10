using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace TcpServer
{
    public class Application
    {
        private const int Port = 8081;
        private static int _counter;

        static async Task Main(string[] args)
        {
            var listener = new TcpListener(IPAddress.Loopback, Port);
            listener.Start();
            Console.WriteLine($"Listening on {Port}");
            while (true)
            {
                var client = await listener.AcceptTcpClientAsync();
                var id = _counter++;
                Console.WriteLine($"connection accepted with id '{id}'");
                Handle(id, client);
            }
        }

        private static readonly JsonSerializer Serializer = new JsonSerializer();

        private static async void Handle(int id, TcpClient client)
        {
            using (client)
            {
                var stream = client.GetStream();
                var reader = new JsonTextReader(new StreamReader(stream))
                {
                    // To support reading multiple top-level objects
                    SupportMultipleContent = true
                };
                var writer = new JsonTextWriter(new StreamWriter(stream));
                while (true)
                {
                    try
                    {
                        // to consume any bytes until start of object ('{')
                        do
                        {
                            await reader.ReadAsync();
                            Console.WriteLine($"advanced to {reader.TokenType}");
                        } while (reader.TokenType != JsonToken.StartObject
                                 && reader.TokenType != JsonToken.None);

                        if (reader.TokenType == JsonToken.None)
                        {
                            Console.WriteLine($"[{id}] reached end of input stream, ending.");
                            return;
                        }

                        var json = await JObject.LoadAsync(reader);
                        // to ensure that proper deserialization is possible
                        json.ToObject<Request>();
                        var response = new Response
                        {
                            Status = 200,
                            Payload = json,
                        };
                        Serializer.Serialize(writer, response);
                        await writer.FlushAsync();
                    }
                    catch (JsonReaderException e)
                    {
                        Console.WriteLine($"[{id}] Error reading JSON: {e.Message}, continuing");
                        var response = new Response
                        {
                            Status = 400,
                        };
                        Serializer.Serialize(writer, response);
                        await writer.FlushAsync();
                        // close the connection because an error may not be recoverable by the reader
                        return;
                    }
                    catch (Exception e)
                    {
                        Console.WriteLine($"[{id}] Unexpected exception, closing connection {e.Message}");
                        return;
                    }
                }
            }
        }
    }
}