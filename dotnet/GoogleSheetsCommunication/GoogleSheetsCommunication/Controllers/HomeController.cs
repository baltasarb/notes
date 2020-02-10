namespace GoogleSheetsCommunication.Controllers
{
    using System.Diagnostics;
    using Microsoft.AspNetCore.Mvc;
    using Microsoft.Extensions.Logging;
    using GoogleSheetsCommunication.Models;
    using GoogleSheetsCommunication.Services;

    public class HomeController : Controller
    {
        private readonly ILogger<HomeController> _logger;

        public HomeController(ILogger<HomeController> logger)
        {
            _logger = logger;
        }

        public IActionResult Index()
        {
            return View();
        }

        public IActionResult Privacy()
        {
            var gs = new GoogleSheetService();

            //gs.UnauthenticatedSheetReadingSample_ApiKey();

            //gs.ServiceAccountSheetReadingSample();

            //gs.ServiceAccountSheetReadingSample();

            gs.AuthenticationSheetReadingSample();

            return View();
        }

        [ResponseCache(Duration = 0, Location = ResponseCacheLocation.None, NoStore = true)]
        public IActionResult Error()
        {
            return View(new ErrorViewModel { RequestId = Activity.Current?.Id ?? HttpContext.TraceIdentifier });
        }
    }
}
