package od.configutil;

/**
 * Created by IntelliJ IDEA.
* User: Nick Ebbutt
* Date: 29-Apr-2010
* Time: 14:38:49
*/
public final class Migration {
    private long targetVersion;
    private String migrationClass;
    private String[] arguments;

    public Migration(long targetVersion, String migrationClass, String[] arguments) {
        this.targetVersion = targetVersion;
        this.migrationClass = migrationClass;
        this.arguments = arguments;
    }

    public Migration() {
    }

    public long getTargetVersion() {
        return targetVersion;
    }

    public void setTargetVersion(long targetVersion) {
        this.targetVersion = targetVersion;
    }

    public String getMigrationClass() {
        return migrationClass;
    }

    public void setMigrationClass(String migrationClass) {
        this.migrationClass = migrationClass;
    }

    public String[] getArguments() {
        return arguments;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }
}
