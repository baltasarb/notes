using Newtonsoft.Json.Linq;

namespace TcpServer
{
    public static class ResponseFactory
    {
        private static class StatusCodes
        {
            public const int Success = 200;
            public const int Timeout = 204;
            public const int InvalidRequest = 400;
            public const int QueueDoesNotExist = 404;
            public const int ServerError = 500;
            public const int ServiceUnavailable = 503;
        }

        private static Response GenericResponse(int status, string details)
        {
            return new Response()
            {
                Status = status,
                Headers = null,
                Payload = null,
                Details = details
            };
        }

        private static Response ResponseWithPayload(int status, JObject payload, string details)
        {
            return new Response()
            {
                Status = status,
                Headers = null,
                Payload = payload,
                Details = details
            };
        }

        public static Response SuccessResponse(string details = "Operation successful.")
        {
            return GenericResponse(StatusCodes.Success, details);
        }

        public static Response SuccessResponse(JObject payload)
        {
            return ResponseWithPayload(StatusCodes.Success, payload, "Received payload with success.");
        }

        public static Response TimeoutResponse(string details = "Waiting time expired.")
        {
            return GenericResponse(StatusCodes.Timeout, details);
        }

        public static Response InvalidRequestResponse(string details = "The request format is incorrect.")
        {
            return GenericResponse(StatusCodes.InvalidRequest, details);
        }

        public static Response QueueDoesNotExistResponse()
        {
            return GenericResponse(StatusCodes.QueueDoesNotExist, "The queue in the parameter path does not exist.");
        }

        public static Response ServerErrorResponse(string details = "Server error.")
        {
            return GenericResponse(StatusCodes.ServerError, details);
        }

        public static Response ServiceUnavailableResponse(
            string details = "Service is shutting down and unavailable for further requests.")
        {
            return GenericResponse(StatusCodes.ServiceUnavailable,
                details);
        }
    }
}