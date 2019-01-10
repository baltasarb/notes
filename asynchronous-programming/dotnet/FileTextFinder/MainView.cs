using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace FileTextFinder
{
    public partial class MainView : Form
    {
        private readonly string _searchingStatusMessage = "Searching...";
        private readonly string _stoppedStatusMessage = "Stopped.";

        private CancellationTokenSource _cancellationTokenSource;

        private delegate void UpdateListRowCallback(int index, string result);

        public MainView()
        {
            InitializeComponent();
        }

        private async void beginButton_Click(object sender, EventArgs e)
        {
            //avoids operation initialization while current one is executing
            beginButton.Enabled = false;

            var folderPath = folderPathTextBox.Text;
            var textToFind = textToFindTextBox.Text;

            try
            {
                ValidateInputsOrThrowException(folderPath, textToFind);

                //enables cancellation, clears current list, renews the token source
                PrepareSearch();

                //get the file paths from the given folder
                var filePaths = await FileUtils.GetFilePathsFromFolderAsync(folderPath);

                //search the string in the found files
                await FindTextInFolderFiles(filePaths, textToFind);
            }
            catch (Exception ex)
            {
                ClearSearchingTextFromListItems();
                ExceptionHandler.HandleException(ex, ShowInvalidStateMessage);
            }
            finally
            {
                beginButton.Enabled = true;
                DisableCancelButton();
            }
        }

        private void cancelButton_Click(object sender, EventArgs e)
        {
            _cancellationTokenSource.Cancel();
        }

        //enables cancellation, clears current list, renews the token source
        private void PrepareSearch()
        {
            resultList.Items.Clear();
            EnableCancelButton();
            _cancellationTokenSource = new CancellationTokenSource();
        }

        private async Task FindTextInFolderFiles(string[] filePaths, string textToFind)
        {
            var fileNames = FileUtils.GetFileNamesFromFilePaths(filePaths);
            AddFileNamesToResultsListView(fileNames);

            await FileUtils.FindTextOccurrencesInMultipleFiles(
                filePaths,
                textToFind,
                _cancellationTokenSource.Token,
                (index, toUpdate) =>
                    resultList.BeginInvoke(new UpdateListRowCallback(UpdateListViewRow), index, toUpdate)
            );
        }

        private static void ValidateInputsOrThrowException(string folderPath, string textToFind)
        {
            if(!folderPath.Any() || !textToFind.Any()) throw new InvalidInputException();
        }

        //provides feedback on cancellation and error occurrences
        private static void ShowInvalidStateMessage(string message)
        {
            MessageBox.Show(message);
        }

        private void ClearSearchingTextFromListItems()
        {
            var listItems = resultList.Items;

            foreach (ListViewItem listItem in listItems)
            {
                var linesColumnRow = listItem.SubItems[1];
                if (linesColumnRow.Text == _searchingStatusMessage)
                    linesColumnRow.Text = _stoppedStatusMessage;
            }
        }

        private void AddFileNamesToResultsListView(IEnumerable<string> fileNames)
        {
            foreach (var fileName in fileNames)
            {
                AddFileNameToRow(fileName);
            }
        }

        private void AddFileNameToRow(string fileName)
        {
            var itemListView = new ListViewItem(fileName);
            itemListView.SubItems.Add(_searchingStatusMessage);
            resultList.Items.Add(itemListView);
        }

        private void UpdateListViewRow(int index, string textToInsert)
        {
            if (!textToInsert.Any())
            {
                textToInsert = "Not found.";
            }

            var item = resultList.Items[index];
            item.SubItems[1].Text = textToInsert;
        }

        private void DisableCancelButton()
        {
            cancelButton.Enabled = false;
        }

        private void EnableCancelButton()
        {
            cancelButton.Enabled = true;
        }

        private void button1_Click(object sender, EventArgs e)
        {
            FolderBrowserDialog folderDlg = new FolderBrowserDialog {ShowNewFolderButton = true};

            // Show the FolderBrowserDialog.  
            DialogResult result = folderDlg.ShowDialog();
            if (result == DialogResult.OK)
            {
                folderPathTextBox.Text = folderDlg.SelectedPath;
            }
        }
    }
}