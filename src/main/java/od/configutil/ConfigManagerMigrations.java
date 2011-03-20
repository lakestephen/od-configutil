package od.configutil;

import java.util.ArrayList;

/**
* Created by IntelliJ IDEA.
* User: Nick Ebbutt
* Date: 29-Apr-2010
* Time: 14:38:13
*
* A bean to contain a list of migrations, for serialization
*/
public class ConfigManagerMigrations {

    private ArrayList<Migration> migrationList = new ArrayList<Migration>();

    public ArrayList<Migration> getMigrationList() {
        return migrationList;
    }

    public void setMigrationList(ArrayList<Migration> migrationList) {
        this.migrationList = migrationList;
    }

    public void addMigration(Migration c) {
        migrationList.add(c);
    }
}
