using Newtonsoft.Json.Linq;

namespace TcpServer
{
    public interface IServerOperations
    {
        //guarantees the existence of the queue with the name defined in the parameter path.
        Response Create(Request request);

        //sends the message contained in the payload to the queue with the name defined in the parameter path.
        Response Send(Request request);

        //removes a message from the queue with the name given by the parameter path and returns it in the
        //parameter payload. The maximum waiting time, in milliseconds,is defined in the headers (timeout).
        Response Receive(Request request, out JObject payload);

        //Initializes the shutdown process of the server, waiting for it to be concluded.
        //The maximum waiting time, in milliseconds,is defined in the headers (timeout).
        Response Shutdown(Request request);
    }
}