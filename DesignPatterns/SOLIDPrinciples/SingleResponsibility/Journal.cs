namespace SOLIDPrinciples.SingleResponsibility
{
    using System;
    using System.Collections.Generic;

    class Journal
    {
        private readonly Dictionary<int, string> entries = new Dictionary<int, string>();
        private static int count = 0;

        public int AddEntry(string text)
        {
            entries.Add(count, text);
            return count++;
        }

        public void RemoveEntry(int index)
        {
            entries.Remove(index);
        }

        public override string ToString()
        {
            return string.Join(Environment.NewLine, entries);
        }

        ///*
        // * Both Save and Load are out of the scope of this class and should be separated, 
        // * changed into a class of their own
        // * */
        //public void Save(string filename)
        //{
        //    try
        //    {
        //        File.WriteAllText(filename, ToString());
        //    }
        //    catch (IOException ex)
        //    {
        //        Console.WriteLine("Error saving into file: {0}.\n Error:{1}.", filename, ex);
        //    }
        //}

        //public void Load()
        //{
        //    //Load the file from the disk
        //}
    }
}
