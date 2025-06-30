using Npgsql; 

namespace Autoparts.DB
{
    public static class DBConnection
    {
        private static readonly string connectionString =
            "Host=localhost;Port=5432;Username=postgres;Password=root;Database=autoparts_db";

        public static NpgsqlConnection GetConnection()
        {
            return new NpgsqlConnection(connectionString);
        }
    }
}
