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


        public static Response SuccessResponse()
        {
            return new Response()
            {
                Status = StatusCodes.Success,
                Headers = null,
                Payload = null
            };
        }

        public static Response TimeoutResponse()
        {
            return new Response()
            {
                Status = StatusCodes.Timeout,
                Headers = null,
                Payload = null
            };
        }

        public static Response InvalidRequestResponse()
        {
            return new Response()
            {
                Status = StatusCodes.InvalidRequest,
                Headers = null,
                Payload = null
            };
        }

        public static Response QueueDoesNotExistResponse()
        {
            return new Response()
            {
                Status = StatusCodes.QueueDoesNotExist,
                Headers = null,
                Payload = null
            };
        }

        public static Response ServerErrorResponse()
        {
            return new Response()
            {
                Status = StatusCodes.ServerError,
                Headers = null,
                Payload = null
            };
        }

        public static Response ServiceUnavailableResponse()
        {
            return new Response()
            {
                Status = StatusCodes.ServiceUnavailable,
                Headers = null,
                Payload = null
            };
        }
    }
}