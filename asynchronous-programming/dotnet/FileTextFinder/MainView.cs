using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
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
            //avoids operation initialization during previous one
            beginButton.Enabled = false;

            var folderPath = folderPathTextBox.Text;
            var textToFind = textToFindTextBox.Text;

            if (InputsAreInvalid(folderPath, textToFind))
            {
                ShowInvalidStateMessage("Missing input, both fields are required.");
                return;
            }

            //enables cancellation, clears current list, renews the token source
            PrepareSearch();

            try
            {
                //get the file paths from the given folder
                var filePaths = await FileUtils.GetFilePathsFromFolderAsync(folderPath);
                if (!filePaths.Any())
                {
                    ShowInvalidStateMessage("No files were found in the selected folder.");
                    return;
                }

                //search the string in the found files
                FindTextInFolderFiles(filePaths, textToFind);
            }
            catch (Exception ex)
            {
                if (ex is OperationCanceledException)
                {
                    ClearSearchingTextFromListItems();
                }

                ExceptionHandler.HandleException(ex, ShowInvalidStateMessage);
                DisableCancelButton();
                beginButton.Enabled = true;
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

        private async void FindTextInFolderFiles(string[] filePaths, string textToFind)
        {
            var fileNames = FileUtils.GetFileNamesFromFilePaths(filePaths);
            AddFileNamesToResultsListView(fileNames);
            try
            {
                await FileUtils.FindTextOccurrencesInMultipleFiles(
                    filePaths,
                    textToFind,
                    _cancellationTokenSource.Token,
                    (index, toUpdate) =>
                        resultList.BeginInvoke(new UpdateListRowCallback(UpdateListViewRow), index, toUpdate)
                );
            }
            catch (Exception e)
            {
                ExceptionHandler.HandleException(e, ShowInvalidStateMessage);
            }

            beginButton.Enabled = true;
            DisableCancelButton();
        }

        private static bool InputsAreInvalid(string folderPath, string textToFind)
        {
            return !folderPath.Any() || !textToFind.Any();
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