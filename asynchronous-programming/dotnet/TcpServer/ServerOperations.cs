using Newtonsoft.Json.Linq;

namespace TcpServer
{
    public interface IServerOperations
    {
        //guarantees the existence of the queue with the name defined in the parameter path.
        void Create(string path);

        //sends the message contained int the payload to the queue with the name defined in the parameter path.
        void Send(string path, JObject payload);

        //removes a message from the queue with the name given by the parameter path and returns it in the
        //parameter payload. The maximum waiting time, in milliseconds,is defined in the headers (timeout).
        void Receive(string path, out JObject payload, long timeout);

        //Initializes the shutdown process of the server, waiting for it to be concluded.
        //The maximum waiting time, in milliseconds,is defined in the headers (timeout).
        void Shutdown(JObject payload);
    }
}