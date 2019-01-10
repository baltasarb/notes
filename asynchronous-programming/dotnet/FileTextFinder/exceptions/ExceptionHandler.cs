using System;
using System.Collections.Generic;

namespace FileTextFinder
{
    public class ExceptionHandler
    {
        //error messages by exception name
        private static readonly Dictionary<string, string> ErrorMessages;

        static ExceptionHandler()
        {
            ErrorMessages = new Dictionary<string, string>
            {
                {"OperationCanceledException", "The operation was cancelled."},
                {"ArgumentException", "The path is invalid"},
                {"ArgumentNullException", "The path is invalid."},
                {"ArgumentOutOfRangeException", "Invalid argument."},
                {"UnauthorizedAccessException", "Access unauthorized."},
                {"DirectoryNotFoundException", "The specified directory was not found."},
                {"PathTooLongException()", "The directory path is too long."},
                {"IOException()", "Error reading files."},
                {"EmptyFolderException", "No files were found in the selected folder."},
                {"InvalidInputException", "Missing input, both fields are required."}
            };
        }

        public static void HandleException(Exception exception, Action<string> showErrorMessageCallback)
        {
            ErrorMessages.TryGetValue(exception.GetType().Name, out var errorMessage);
            showErrorMessageCallback(errorMessage ?? "An unknown error has occurred. " + exception.GetType().Name);
        }
    }
}