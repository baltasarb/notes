using System;
using System.Collections.Generic;

namespace TcpServer
{
    public static class ExceptionHandler
    {
        private static readonly Dictionary<string, string> ExceptionMessages = new Dictionary<string, string>
        {
            {"JsonReaderException", ""}
        };

        public static void HandleException(Exception e)
        {
            string exceptionMessage = ExceptionMessages[e.GetType().Name];

            //todo do something
        }
    }
}