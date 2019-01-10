namespace FileTextFinder
{
    partial class MainView
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.beginButton = new System.Windows.Forms.Button();
            this.folderPathLabel = new System.Windows.Forms.Label();
            this.textToFindLabel = new System.Windows.Forms.Label();
            this.folderPathTextBox = new System.Windows.Forms.TextBox();
            this.textToFindTextBox = new System.Windows.Forms.TextBox();
            this.resultList = new System.Windows.Forms.ListView();
            this.fileNameColumnHeader = ((System.Windows.Forms.ColumnHeader)(new System.Windows.Forms.ColumnHeader()));
            this.fileLinesColumnHeader = ((System.Windows.Forms.ColumnHeader)(new System.Windows.Forms.ColumnHeader()));
            this.cancelButton = new System.Windows.Forms.Button();
            this.folderBrowserDialog1 = new System.Windows.Forms.FolderBrowserDialog();
            this.fileExplorerButton = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // beginButton
            // 
            this.beginButton.Location = new System.Drawing.Point(420, 32);
            this.beginButton.Name = "beginButton";
            this.beginButton.Size = new System.Drawing.Size(99, 22);
            this.beginButton.TabIndex = 0;
            this.beginButton.Text = "Begin";
            this.beginButton.UseVisualStyleBackColor = true;
            this.beginButton.Click += new System.EventHandler(this.beginButton_Click);
            // 
            // folderPathLabel
            // 
            this.folderPathLabel.AutoSize = true;
            this.folderPathLabel.Location = new System.Drawing.Point(47, 18);
            this.folderPathLabel.Name = "folderPathLabel";
            this.folderPathLabel.Size = new System.Drawing.Size(60, 13);
            this.folderPathLabel.TabIndex = 1;
            this.folderPathLabel.Text = "Folder path";
            // 
            // textToFindLabel
            // 
            this.textToFindLabel.AutoSize = true;
            this.textToFindLabel.Location = new System.Drawing.Point(300, 18);
            this.textToFindLabel.Name = "textToFindLabel";
            this.textToFindLabel.Size = new System.Drawing.Size(60, 13);
            this.textToFindLabel.TabIndex = 2;
            this.textToFindLabel.Text = "Text to find";
            // 
            // folderPathTextBox
            // 
            this.folderPathTextBox.Location = new System.Drawing.Point(50, 34);
            this.folderPathTextBox.Name = "folderPathTextBox";
            this.folderPathTextBox.Size = new System.Drawing.Size(153, 20);
            this.folderPathTextBox.TabIndex = 3;
            // 
            // textToFindTextBox
            // 
            this.textToFindTextBox.Location = new System.Drawing.Point(303, 34);
            this.textToFindTextBox.Name = "textToFindTextBox";
            this.textToFindTextBox.Size = new System.Drawing.Size(100, 20);
            this.textToFindTextBox.TabIndex = 4;
            // 
            // resultList
            // 
            this.resultList.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
            this.fileNameColumnHeader,
            this.fileLinesColumnHeader});
            this.resultList.GridLines = true;
            this.resultList.HeaderStyle = System.Windows.Forms.ColumnHeaderStyle.Nonclickable;
            this.resultList.LabelWrap = false;
            this.resultList.Location = new System.Drawing.Point(50, 61);
            this.resultList.Name = "resultList";
            this.resultList.Size = new System.Drawing.Size(699, 363);
            this.resultList.TabIndex = 11;
            this.resultList.UseCompatibleStateImageBehavior = false;
            this.resultList.View = System.Windows.Forms.View.Details;
            // 
            // fileNameColumnHeader
            // 
            this.fileNameColumnHeader.Text = "File name";
            this.fileNameColumnHeader.Width = 100;
            // 
            // fileLinesColumnHeader
            // 
            this.fileLinesColumnHeader.Text = "File lines";
            this.fileLinesColumnHeader.Width = 593;
            // 
            // cancelButton
            // 
            this.cancelButton.Enabled = false;
            this.cancelButton.Location = new System.Drawing.Point(525, 32);
            this.cancelButton.Name = "cancelButton";
            this.cancelButton.Size = new System.Drawing.Size(99, 22);
            this.cancelButton.TabIndex = 12;
            this.cancelButton.Text = "Cancel";
            this.cancelButton.UseVisualStyleBackColor = true;
            this.cancelButton.Click += new System.EventHandler(this.cancelButton_Click);
            // 
            // fileExplorerButton
            // 
            this.fileExplorerButton.Location = new System.Drawing.Point(209, 32);
            this.fileExplorerButton.Name = "fileExplorerButton";
            this.fileExplorerButton.Size = new System.Drawing.Size(77, 23);
            this.fileExplorerButton.TabIndex = 13;
            this.fileExplorerButton.Text = "File Explorer";
            this.fileExplorerButton.UseVisualStyleBackColor = true;
            this.fileExplorerButton.Click += new System.EventHandler(this.button1_Click);
            // 
            // MainView
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(800, 450);
            this.Controls.Add(this.fileExplorerButton);
            this.Controls.Add(this.cancelButton);
            this.Controls.Add(this.resultList);
            this.Controls.Add(this.textToFindTextBox);
            this.Controls.Add(this.folderPathTextBox);
            this.Controls.Add(this.textToFindLabel);
            this.Controls.Add(this.folderPathLabel);
            this.Controls.Add(this.beginButton);
            this.Name = "MainView";
            this.Text = "Form1";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button beginButton;
        private System.Windows.Forms.Label folderPathLabel;
        private System.Windows.Forms.Label textToFindLabel;
        private System.Windows.Forms.TextBox folderPathTextBox;
        private System.Windows.Forms.TextBox textToFindTextBox;
        private System.Windows.Forms.ListView resultList;
        private System.Windows.Forms.ColumnHeader fileNameColumnHeader;
        private System.Windows.Forms.ColumnHeader fileLinesColumnHeader;
        private System.Windows.Forms.Button cancelButton;
        private System.Windows.Forms.FolderBrowserDialog folderBrowserDialog1;
        private System.Windows.Forms.Button fileExplorerButton;
    }
}

