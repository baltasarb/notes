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

        public static Response SuccessResponse()
        {
            return GenericResponse(StatusCodes.Success, "Operation successful");
        }

        public static Response SuccessResponse(JObject payload)
        {
            return ResponseWithPayload(StatusCodes.Success, payload, "Received payload with success.");
        }

        public static Response TimeoutResponse()
        {
            return GenericResponse(StatusCodes.Timeout, "Waiting time expired.");
        }

        public static Response InvalidRequestResponse()
        {
            return GenericResponse(StatusCodes.InvalidRequest, "The request format is incorrect.");
        }

        public static Response QueueDoesNotExistResponse()
        {
            return GenericResponse(StatusCodes.QueueDoesNotExist, "The queue in the parameter path does not exist.");
        }

        public static Response ServerErrorResponse()
        {
            return GenericResponse(StatusCodes.ServerError, "Server error.");
        }

        public static Response ServiceUnavailableResponse()
        {
            return GenericResponse(StatusCodes.ServiceUnavailable,
                "Service is shutting down and unavailable for further requests.");
        }
    }
}