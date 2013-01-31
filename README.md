od-configutil
=============

Object Definitions Config Util - a utility to help load and migrate configs

 ConfigManager is a utility for loading and saving config files, which also supports config file versioning and 
 migrations between versions in a highly customisable manner.
 
  Key Concepts:
  Loading of configs takes place from a ConfigSource
  Saving of configs takes place to a ConfigSink
 
  When saving a config, the ConfigManager expects to receive a Java class instance representing config data
  The bean will then be serialized using the configured ConfigSerializer (this may perform serialization
  to XML using XStream, for example). The serialized String data output will then be written to the ConfigSink
 
  Versioning of configs is also handled by ConfigManager. Associated with ConfigManager is a migrationSource.
  This defines a list of Config Migrations each of which map to a configuration id. When saving a config, in
  general the ConfigSink will persist the config data along with the most recent configuration id as supplied by
  the configured migrationSource.
 
  When loading a config, both the saved config data and the saved configuration id are loaded by the ConfigSource.
  Before deserialization takes place, any config migrations from the migrationSource which represent more recent
  versions of the configuration are used to transform the loaded configuration into a version consistent with the
  most recent configuration id. For example, if the persisted config contained XML which referenced a renamed class,
  a regular expression migration might be used to migrate the class name for the more recent config version. Once any
  migrations have been applied, the config data is then deserialized back into a Java object by the configSerializer.
  A ClasspathMigrationLoader is supplied, which loads config migrations from an xml file on the classpath, typically
  from /configMigrations.xml
 
  All the above elements, the ConfigSource, ConfigSink, MigrationSource and ConfigSerialzier are customisable.
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
