using System.Collections.Generic;

namespace SOLIDPrinciples.OpenClosed
{
    class ProductFilter
    {
        public static IEnumerable<Product> FilterBySize(IEnumerable<Product> products, Size size)
        {
            foreach (var product in products)
            {
                if (product.Size == size)
                {
                    yield return product;
                }
            }
        }
    }
}
