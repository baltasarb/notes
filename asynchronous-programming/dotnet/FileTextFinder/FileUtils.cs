using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace FileTextFinder
{
    public class FileUtils
    {
        public static async Task<string[]> GetFilePathsFromFolderAsync(string folderPath, string fileType = "*.txt")
        {
            return await Task.Run(() =>
                {
                    var result = Directory.GetFiles(folderPath, fileType, SearchOption.TopDirectoryOnly);
                    if (!result.Any())
                    {
                        throw new EmptyFolderException();
                    }
                    return result;
                }
            );
        }

        public static IEnumerable<string> GetFileNamesFromFilePaths(IReadOnlyList<string> filePaths)
        {
            var fileNames = new string[filePaths.Count];

            for (int i = 0; i < fileNames.Length; i++)
            {
                fileNames[i] = GetFileNameFromFilePath(filePaths[i]);
            }

            return fileNames;
        }

        private static string GetFileNameFromFilePath(string filePath)
        {
            var splitPath = filePath.Split(new[] {"\\"}, StringSplitOptions.None);
            return splitPath[splitPath.Length - 1];
        }

        public static string FindTextOccurrencesInFile(string filePath, string toFind)
        {
            IEnumerable<string> lines = File.ReadAllLines(filePath);
            lines = lines.Where(line => line.Contains(toFind));
            return string.Join(", ", lines);
        }

        public static async Task FindTextOccurrencesInMultipleFiles(string[] filePaths, string textToFind,
            CancellationToken cancellationToken, Action<int, string> updateLineCallback)
        {
            await Task.Run(() =>
                {
                    var options = new ParallelOptions {CancellationToken = cancellationToken};

                    options.CancellationToken.ThrowIfCancellationRequested();


                    Parallel.For(
                        0,
                        filePaths.Length,
                        options,
                        (index, state) =>
                        {
                            options.CancellationToken.ThrowIfCancellationRequested();
                            var result = FindTextOccurrencesInFile(filePaths[index], textToFind);
                            updateLineCallback(index, result);
                        });
                },
                cancellationToken);
        }
    }
}