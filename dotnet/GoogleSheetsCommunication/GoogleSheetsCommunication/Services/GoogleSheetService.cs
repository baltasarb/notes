namespace GoogleSheetsCommunication.Services
{
    using Google.Apis.Auth.OAuth2;
    using Google.Apis.Services;
    using Google.Apis.Sheets.v4;
    using Google.Apis.Sheets.v4.Data;
    using System;
    using System.Collections.Generic;
    using System.IO;
    using System.Threading.Tasks;

    public class GoogleSheetService : IGoogleSheetService
    {
        private const string ErdnaseSpreadsheetId = "18I2eWy6qAdEcsmdoV2zupMuzzqsioCsSjjEqyOwBKkc";
        private const string LazarusSpreadsheetId = "1kfCf59947tfKVGKULPmyk1YJ17IK_Ontgj4LcMWQjlM";
        private const string ApplicationName = "peg-onboarding";
        private readonly SheetsService SheetsService;

        public GoogleSheetService()
        {
            //should it be initialized once or per request
            this.SheetsService = InitializeSheetsService();
        }

        async Task CreatePartnerhipAsync()
        {
            await Task.Delay(10);
        }

        async Task EnrollUser(string partnershipId, long userId)
        {
            await Task.Delay(10);
        }

        async Task AddPartnershipPermissions(string partnershipId, IEnumerable<string> emails)
        {
            await Task.Delay(10);
        }

        public void ApiAuthenticationSheetReadingSample()
        {
            var service = new SheetsService(new BaseClientService.Initializer
            {
                ApplicationName = ApplicationName,
                ApiKey = "",
            });

            var range = "Sheet1!A1:B2";
            SpreadsheetsResource.ValuesResource.GetRequest request =
                    service.Spreadsheets.Values.Get(ErdnaseSpreadsheetId, range);

            // Prints the names and majors of students in a sample spreadsheet:
            // https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
            ValueRange response = request.Execute();
            IList<IList<Object>> values = response.Values;
        }

        public void AuthenticationSheetReadingSample()
        {
            var range = "Sheet1!A1:B2";
            SpreadsheetsResource.ValuesResource.GetRequest request =
                    SheetsService.Spreadsheets.Values.Get(LazarusSpreadsheetId, range);

            ValueRange response = request.Execute();
            IList<IList<Object>> values = response.Values;
        }

        private SheetsService InitializeSheetsService()
        {
            var credentials = BuildCredentials();

            return new SheetsService(new BaseClientService.Initializer()
            {
                HttpClientInitializer = credentials,
                ApplicationName = ApplicationName
            });
        }

        private GoogleCredential BuildCredentials()
        {
            GoogleCredential credentials;
            string[] scopes = { SheetsService.Scope.Spreadsheets }; // Change this if you're accessing Drive or Docs

            // Put your credentials json file in the root of the solution and make sure copy to output dir property is set to always copy 
            using (var stream = new FileStream(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "credentials.json"),
                FileMode.Open, FileAccess.Read))
            {
                credentials = GoogleCredential.FromStream(stream).CreateScoped(scopes);
            }

            return credentials;
        }    
    }
}
