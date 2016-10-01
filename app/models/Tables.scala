package models
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.driver.MySQLDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.driver.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Directories.schema ++ Files.schema ++ PlayEvolutions.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Directories
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param parentId Database column parent_id SqlType(INT), Default(None)
   *  @param userIds Database column user_ids SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param collegeCodes Database column college_codes SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param name Database column name SqlType(VARCHAR), Length(50,true)
   *  @param insertedBy Database column inserted_by SqlType(INT)
   *  @param insertedAt Database column inserted_at SqlType(DATETIME)
   *  @param updatedAt Database column updated_at SqlType(DATETIME) */
  case class DirectoriesRow(id: Int, parentId: Option[Int] = None, userIds: Option[String] = None, collegeCodes: Option[String] = None, name: String, insertedBy: Int, insertedAt: java.sql.Timestamp, updatedAt: java.sql.Timestamp)
  /** GetResult implicit for fetching DirectoriesRow objects using plain SQL queries */
  implicit def GetResultDirectoriesRow(implicit e0: GR[Int], e1: GR[Option[Int]], e2: GR[Option[String]], e3: GR[String], e4: GR[java.sql.Timestamp]): GR[DirectoriesRow] = GR{
    prs => import prs._
    DirectoriesRow.tupled((<<[Int], <<?[Int], <<?[String], <<?[String], <<[String], <<[Int], <<[java.sql.Timestamp], <<[java.sql.Timestamp]))
  }
  /** Table description of table directories. Objects of this class serve as prototypes for rows in queries. */
  class Directories(_tableTag: Tag) extends Table[DirectoriesRow](_tableTag, "directories") {
    def * = (id, parentId, userIds, collegeCodes, name, insertedBy, insertedAt, updatedAt) <> (DirectoriesRow.tupled, DirectoriesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), parentId, userIds, collegeCodes, Rep.Some(name), Rep.Some(insertedBy), Rep.Some(insertedAt), Rep.Some(updatedAt)).shaped.<>({r=>import r._; _1.map(_=> DirectoriesRow.tupled((_1.get, _2, _3, _4, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column parent_id SqlType(INT), Default(None) */
    val parentId: Rep[Option[Int]] = column[Option[Int]]("parent_id", O.Default(None))
    /** Database column user_ids SqlType(VARCHAR), Length(255,true), Default(None) */
    val userIds: Rep[Option[String]] = column[Option[String]]("user_ids", O.Length(255,varying=true), O.Default(None))
    /** Database column college_codes SqlType(VARCHAR), Length(255,true), Default(None) */
    val collegeCodes: Rep[Option[String]] = column[Option[String]]("college_codes", O.Length(255,varying=true), O.Default(None))
    /** Database column name SqlType(VARCHAR), Length(50,true) */
    val name: Rep[String] = column[String]("name", O.Length(50,varying=true))
    /** Database column inserted_by SqlType(INT) */
    val insertedBy: Rep[Int] = column[Int]("inserted_by")
    /** Database column inserted_at SqlType(DATETIME) */
    val insertedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("inserted_at")
    /** Database column updated_at SqlType(DATETIME) */
    val updatedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_at")
  }
  /** Collection-like TableQuery object for table Directories */
  lazy val Directories = new TableQuery(tag => new Directories(tag))

  /** Entity class storing rows of table Files
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param parentId Database column parent_id SqlType(INT), Default(None)
   *  @param userIds Database column user_ids SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param collegeCodes Database column college_codes SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param name Database column name SqlType(VARCHAR), Length(50,true)
   *  @param path Database column path SqlType(VARCHAR), Length(255,true)
   *  @param insertedBy Database column inserted_by SqlType(INT)
   *  @param insertedAt Database column inserted_at SqlType(DATETIME)
   *  @param updatedAt Database column updated_at SqlType(DATETIME) */
  case class FilesRow(id: Int, parentId: Option[Int] = None, userIds: Option[String] = None, collegeCodes: Option[String] = None, name: String, path: String, insertedBy: Int, insertedAt: java.sql.Timestamp, updatedAt: java.sql.Timestamp)
  /** GetResult implicit for fetching FilesRow objects using plain SQL queries */
  implicit def GetResultFilesRow(implicit e0: GR[Int], e1: GR[Option[Int]], e2: GR[Option[String]], e3: GR[String], e4: GR[java.sql.Timestamp]): GR[FilesRow] = GR{
    prs => import prs._
    FilesRow.tupled((<<[Int], <<?[Int], <<?[String], <<?[String], <<[String], <<[String], <<[Int], <<[java.sql.Timestamp], <<[java.sql.Timestamp]))
  }
  /** Table description of table files. Objects of this class serve as prototypes for rows in queries. */
  class Files(_tableTag: Tag) extends Table[FilesRow](_tableTag, "files") {
    def * = (id, parentId, userIds, collegeCodes, name, path, insertedBy, insertedAt, updatedAt) <> (FilesRow.tupled, FilesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), parentId, userIds, collegeCodes, Rep.Some(name), Rep.Some(path), Rep.Some(insertedBy), Rep.Some(insertedAt), Rep.Some(updatedAt)).shaped.<>({r=>import r._; _1.map(_=> FilesRow.tupled((_1.get, _2, _3, _4, _5.get, _6.get, _7.get, _8.get, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column parent_id SqlType(INT), Default(None) */
    val parentId: Rep[Option[Int]] = column[Option[Int]]("parent_id", O.Default(None))
    /** Database column user_ids SqlType(VARCHAR), Length(255,true), Default(None) */
    val userIds: Rep[Option[String]] = column[Option[String]]("user_ids", O.Length(255,varying=true), O.Default(None))
    /** Database column college_codes SqlType(VARCHAR), Length(255,true), Default(None) */
    val collegeCodes: Rep[Option[String]] = column[Option[String]]("college_codes", O.Length(255,varying=true), O.Default(None))
    /** Database column name SqlType(VARCHAR), Length(50,true) */
    val name: Rep[String] = column[String]("name", O.Length(50,varying=true))
    /** Database column path SqlType(VARCHAR), Length(255,true) */
    val path: Rep[String] = column[String]("path", O.Length(255,varying=true))
    /** Database column inserted_by SqlType(INT) */
    val insertedBy: Rep[Int] = column[Int]("inserted_by")
    /** Database column inserted_at SqlType(DATETIME) */
    val insertedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("inserted_at")
    /** Database column updated_at SqlType(DATETIME) */
    val updatedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_at")
  }
  /** Collection-like TableQuery object for table Files */
  lazy val Files = new TableQuery(tag => new Files(tag))

  /** Entity class storing rows of table PlayEvolutions
   *  @param id Database column id SqlType(INT), PrimaryKey
   *  @param hash Database column hash SqlType(VARCHAR), Length(255,true)
   *  @param appliedAt Database column applied_at SqlType(TIMESTAMP)
   *  @param applyScript Database column apply_script SqlType(MEDIUMTEXT), Length(16777215,true), Default(None)
   *  @param revertScript Database column revert_script SqlType(MEDIUMTEXT), Length(16777215,true), Default(None)
   *  @param state Database column state SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param lastProblem Database column last_problem SqlType(MEDIUMTEXT), Length(16777215,true), Default(None) */
  case class PlayEvolutionsRow(id: Int, hash: String, appliedAt: java.sql.Timestamp, applyScript: Option[String] = None, revertScript: Option[String] = None, state: Option[String] = None, lastProblem: Option[String] = None)
  /** GetResult implicit for fetching PlayEvolutionsRow objects using plain SQL queries */
  implicit def GetResultPlayEvolutionsRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Timestamp], e3: GR[Option[String]]): GR[PlayEvolutionsRow] = GR{
    prs => import prs._
    PlayEvolutionsRow.tupled((<<[Int], <<[String], <<[java.sql.Timestamp], <<?[String], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table play_evolutions. Objects of this class serve as prototypes for rows in queries. */
  class PlayEvolutions(_tableTag: Tag) extends Table[PlayEvolutionsRow](_tableTag, "play_evolutions") {
    def * = (id, hash, appliedAt, applyScript, revertScript, state, lastProblem) <> (PlayEvolutionsRow.tupled, PlayEvolutionsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(hash), Rep.Some(appliedAt), applyScript, revertScript, state, lastProblem).shaped.<>({r=>import r._; _1.map(_=> PlayEvolutionsRow.tupled((_1.get, _2.get, _3.get, _4, _5, _6, _7)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    /** Database column hash SqlType(VARCHAR), Length(255,true) */
    val hash: Rep[String] = column[String]("hash", O.Length(255,varying=true))
    /** Database column applied_at SqlType(TIMESTAMP) */
    val appliedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("applied_at")
    /** Database column apply_script SqlType(MEDIUMTEXT), Length(16777215,true), Default(None) */
    val applyScript: Rep[Option[String]] = column[Option[String]]("apply_script", O.Length(16777215,varying=true), O.Default(None))
    /** Database column revert_script SqlType(MEDIUMTEXT), Length(16777215,true), Default(None) */
    val revertScript: Rep[Option[String]] = column[Option[String]]("revert_script", O.Length(16777215,varying=true), O.Default(None))
    /** Database column state SqlType(VARCHAR), Length(255,true), Default(None) */
    val state: Rep[Option[String]] = column[Option[String]]("state", O.Length(255,varying=true), O.Default(None))
    /** Database column last_problem SqlType(MEDIUMTEXT), Length(16777215,true), Default(None) */
    val lastProblem: Rep[Option[String]] = column[Option[String]]("last_problem", O.Length(16777215,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table PlayEvolutions */
  lazy val PlayEvolutions = new TableQuery(tag => new PlayEvolutions(tag))
}
