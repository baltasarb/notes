namespace SOLIDPrinciples.SingleResponsibility
{
    using System;
    using System.IO;

    class Persistence
    {
        /*
       * Both Save and Load are out of the scope of this class and should be separated, 
       * changed into a class of their own
       * */
        public void Save(string filename)
        {
            try
            {
                File.WriteAllText(filename, ToString());
            }
            catch (IOException ex)
            {
                Console.WriteLine("Error saving into file: {0}.\n Error:{1}.", filename, ex);
            }
        }

        public void Load()
        {
            //Load the file from the disk
        }
    }
}
