 
od-configutil
=============

Object Definitions Config Util - a utility to help load and migrate configs

 ConfigManager is a utility for loading and saving config files, which also supports config file versioning and 
 migrations between versions in a highly customisable manner.
 
 Key Concepts:
 Loading of configs takes place from a ConfigSource
 Saving of configs takes place to a ConfigSink
 
 When saving a config, the ConfigManager expects to receive a Java class instance representing config data
 The bean will then be serialized to a String value using the configured ConfigSerializer (this may perform serialization
 to XML using XStream, for example). The serialized config data will then be written to the ConfigSink
 
 ConfigManager also handles config versions and can migrate older configs to bring them up to date for a newer config
 version. Associated with ConfigManager is a migrationSource. This defines a list of Config Migrations each of which 
 are associated with a Long configuration id. The configuration id is generally based on a date and time in year-first 
 format yyyyMMddHHss (e.g. 201302010830) which allows sorting as a Long value. When saving a config, the ConfigSink 
 will persist the most recent configuration id along with the serialized config data.
  
 The configuration id value represents the time when the config format last changed. Each time a config migration is 
 added (because the configuration format is changing) the developer defining the migration sets the new version id, 
 and defines a migration which can transform the previous version of the serialized config data into the current version. 
 (Using XPathMigrationStrategy for example). A ClasspathMigrationLoader is supplied, which loads config migrations from 
 an xml file on the classpath, typically from /configMigrations.xml
   
 When loading a config, both the saved config data and the saved configuration id are loaded by the ConfigSource.
 Before deserialization takes place, any config migrations from the migrationSource which represent more recent
 versions of the configuration are used in sequence to transform the loaded configuration into a version consistent with the
 most recent configuration id. For example, if the persisted config contained XML which referenced a renamed class,
 a regular expression migration strategy might be used to migrate the class name to reflect the more recent config version. 
 Once any migrations have been applied, the config data is then deserialized back into a Java object by the configSerializer.
 
 All the above elements, the ConfigSource, ConfigSink, MigrationSource and ConfigSerializer are customisable.
 ConfigManger may be configured with a default in each case, but alternative instances can also be passed into
 overloaded save and load method implementations.
 
 One further concept is the configName.
 When saving and loading, a configName is supplied as a parameter to the load and save method.
 The semantics of this varies depending on the actual config source and sink implementations involved. For some sinks, e.g. FileSink
 the configName is not used (other than for logging) since the File with which the sink is created determines file names absolutely.
 For other sink types (e.g. DirectorySink) the configName is relevant. Here the configName is used to identify the config
 as one of several which may co-exist in the same directory. In this case, the configName provides the file name prefix,
 while the suffix is comprised of the latest version id and a file extension. DirectorySink may therefore end up writing a
 directory containing newer and older config files under several different config names. When configs are loaded from a
 DirectorySouce, typically only the most recent config file for the given configName is loaded.
