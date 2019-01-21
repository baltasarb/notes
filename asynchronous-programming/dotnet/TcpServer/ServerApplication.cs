using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
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
        private static Server _server;
        private static readonly JsonSerializer Serializer = new JsonSerializer();

        private const int MaximumConcurrentClients = 2;

        //server shutdown token
        private static readonly CancellationTokenSource CancellationTokenSource = new CancellationTokenSource();

        private static int _connectionIdCounter;

        private static async Task Main()
        {
            _server = new Server(CancellationTokenSource);

            var listener = new TcpListener(IPAddress.Loopback, Port);
            listener.Start();

            Console.WriteLine($"Server started on port: {Port}.");

            var requestTasks = new HashSet<Task>();

            while (!CancellationTokenSource.IsCancellationRequested)
            {
                try
                {
                    //With wait cancellation allows to cancel the acceptTcpClientAsync, if the server is currently
                    //waiting for more connections and someone already shut it down
                    var client = await listener
                        .AcceptTcpClientAsync()
                        .WithWaitCancellation(CancellationTokenSource.Token);

                    //used to let the client know that he is now connected
                    //server "handshake"
                    await WriteResponse(client.GetStream(),
                        ResponseFactory.SuccessResponse("Connection established with success."));

                    Console.WriteLine($"connection accepted with id: {_connectionIdCounter++}.\n");

                    //add the client to a task container "requestTasks" that will be awaited on server shutdown
                    requestTasks.Add(HandleResponse(client, _connectionIdCounter));

                    //if the server limit was reached stop receiving more requests until one of the current ones finishes
                    if (requestTasks.Count == MaximumConcurrentClients)
                    {
                        requestTasks.Remove(await Task.WhenAny(requestTasks));
                    }
                }
                catch (OperationCanceledException)
                {
                    //on cancellation a shutdown is occurring, no need to handle the exception
                }
                catch (Exception e)
                {
                    Console.WriteLine($"Unknown error: {e.Message}");
                }
            }

            //wait for all tasks to be completed except the one that initiated the shutdown
            while (requestTasks.Count > 1)
            {
                requestTasks.Remove(await Task.WhenAny(requestTasks));
            }

            //terminates the timeout on server shutdown
            _server.NotifyServerShutdownIsComplete();

            //Wait for the last task to complete
            await Task.WhenAny(requestTasks);

            listener.Stop();

            Console.Write("Server shutdown successful.\n");
        }

        public static async Task HandleResponse(TcpClient client, int clientId)
        {
            using (client)
            {
                var stream = client.GetStream();
                using (stream)
                {
                    while (true)
                    {
                        Response response;
                        bool status = false;
                        try
                        {
                            //serialize received request
                            var request = await ReadRequest(stream)
                                .WithWaitCancellation(CancellationTokenSource.Token);

                            //log the received request to the console
                            PrintRequest(request, clientId);

                            //send the serialized request to the server
                            response = await _server.ResolveRequest(request);

                            status = response.Status != 200;
                        }
                        catch (OperationCanceledException)
                        {
                            response = status
                                ? ResponseFactory.SuccessResponse("Server shutdown successful.")
                                : ResponseFactory.ServiceUnavailableResponse();
                        }
                        catch (Exception e)
                        {
                            //if an error occurred in the read process, get an error response to give to the client
                            response = ResponseFactory.ServerErrorResponse(e.Message);
                        }

                        //deserialize and send the response to the client
                        await WriteResponse(stream, response);

                        //if the server is in shutdown terminate this client
                        if (!CancellationTokenSource.IsCancellationRequested) continue;

                        stream.Close();

                        return;
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
                throw new Exception();
            }

            var json = await JObject.LoadAsync(reader);
            return json.ToObject<Request>();
        }

        private static async Task WriteResponse(Stream stream, Response response)
        {
            var writer = new JsonTextWriter(new StreamWriter(stream));
            Serializer.Serialize(writer, response);
            await writer.FlushAsync();
        }

        private static void PrintRequest(Request request, int clientId)
        {
            var stringBuilder = new StringBuilder();

            stringBuilder.Append($"Request received from client with id: {clientId}.\n");

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

            stringBuilder.Append(request.Payload == null
                ? "Payload: no payload received.\n"
                : $"Payload: {request.Payload}\n");

            Console.WriteLine(stringBuilder.ToString());
        }
    }
}