namespace GoogleSheetsCommunication.Services
{
    using Google.Apis.Auth.OAuth2;
    using Google.Apis.Services;
    using Google.Apis.Sheets.v4;
    using Google.Apis.Sheets.v4.Data;
    using Google.Apis.Util.Store;
    using System;
    using System.Collections.Generic;
    using System.IO;
    using System.Threading;
    using System.Threading.Tasks;

    public class GoogleSheetService : IGoogleSheetService
    {
        // If modifying these scopes, delete your previously saved credentials
        // at ~/.credentials/sheets.googleapis.com-dotnet-quickstart.json
        static string[] Scopes = { SheetsService.Scope.SpreadsheetsReadonly };
        static string ApplicationName = "Google Sheets API .NET Quickstart";

        async Task  CreatePartnerhipAsync()
        {
            await Task.Delay(10);
        }

        async Task EnrollUser(string partnershipId, long userId)
        {
            await Task.Delay(10);
        }

        public void UnauthenticatedSheetReadingSample()
        {
            var service = new SheetsService(new BaseClientService.Initializer
            {
                ApplicationName = "Application",
                ApiKey = "AIzaSyBsoy6q0r8WpGd7w3LRqkvsafRGsxQvWLg",
            });

            var testSpreadsheetId = "18I2eWy6qAdEcsmdoV2zupMuzzqsioCsSjjEqyOwBKkc";
            var testRange = "Sheet1!A1:B2";
            SpreadsheetsResource.ValuesResource.GetRequest request =
                    service.Spreadsheets.Values.Get(testSpreadsheetId, testRange);

            // Prints the names and majors of students in a sample spreadsheet:
            // https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
            ValueRange response = request.Execute();
            IList<IList<Object>> values = response.Values;
        }


        public void AuthenticatedSheetReadingSample()
        {
            var service = new SheetsService(new BaseClientService.Initializer
            {
                ApplicationName = "Application",
                ApiKey = "AIzaSyBsoy6q0r8WpGd7w3LRqkvsafRGsxQvWLg",
            });

            var testSpreadsheetId = "18I2eWy6qAdEcsmdoV2zupMuzzqsioCsSjjEqyOwBKkc";
            var testRange = "Sheet1!A1:B2";
            SpreadsheetsResource.ValuesResource.GetRequest request =
                    service.Spreadsheets.Values.Get(testSpreadsheetId, testRange);

            // Prints the names and majors of students in a sample spreadsheet:
            // https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
            ValueRange response = request.Execute();
            IList<IList<Object>> values = response.Values;
        }

        public void GoogleSample()
        {
            var service = new SheetsService(new BaseClientService.Initializer
            {
                ApplicationName = "Gservice Test",
                ApiKey = "AIzaSyBsoy6q0r8WpGd7w3LRqkvsafRGsxQvWLg",
            });

            //// Run the request.
            //Console.WriteLine("Executing a list request...");
            //var result = await service.Apis.List().ExecuteAsync();
            var spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
            var range = "Class Data!A2:E";
            SpreadsheetsResource.ValuesResource.GetRequest request =
                    service.Spreadsheets.Values.Get(spreadsheetId, range);

            // Prints the names and majors of students in a sample spreadsheet:
            // https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
            ValueRange response = request.Execute();
            IList<IList<Object>> values = response.Values;

            if (values != null && values.Count > 0)
            {
                Console.WriteLine("Name, Major");
                foreach (var row in values)
                {
                    // Print columns A and E, which correspond to indices 0 and 4.
                    Console.WriteLine("{0}, {1}", row[0], row[4]);
                }
            }
            else
            {
                Console.WriteLine("No data found.");
            }
            Console.Read();
        }
    }
}
