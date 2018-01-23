package cz.incad.cdk;

import cz.incad.kramerius.utils.conf.KConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.logging.Logger;

public class Main {

    public static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            for (String arg : args) {
                Command.valueOf(arg).doCommand();
            }
        } else {
            Arrays.stream(Command.values()).forEach(cmd->{
                System.out.println(cmd.name());
                System.out.println(cmd.desc());
                System.out.println();
            });
        }
    }

    static enum Command {

        CDK {
            @Override
            public void doCommand() throws Exception {
                CDKMigrationParts.OBJECT_AND_STREAMS.doMigrationPart();
            }

            @Override
            public String desc() {
                StringBuilder builder = new StringBuilder();
                builder.append("Migrace z CDK - akubra_fs(##) -> akubra_fs(##/##/##) ").append('\n');
                builder.append("Nutne promenne pro migraci: ").append('\n');

                builder.append("\takubrafs.streams.source").append(" - adresar zdrojoveho akubra_fs").append('\n');
                builder.append("\takubrafs.objects.source").append(" - adresar zdrojoveho akubra_fs").append('\n');

                builder.append("\takubrafs.streams.target").append(" - adresar ciloveho akubra_fs").append('\n');
                builder.append("\takubrafs.objects.target").append(" - adresar ciloveho akubra_fs").append('\n');

                return builder.toString();
            }
        },

        MZK {
            @Override
            public void doCommand() throws Exception {
                Class.forName("org.postgresql.Driver");
                Connection db = null;
                try {
                    String url = KConfiguration.getInstance().getProperty("fedora3.connectionURL");
                    String userName = KConfiguration.getInstance().getProperty("fedora3.connectionUser");
                    String userPass = KConfiguration.getInstance().getProperty("fedora3.connectionPass");

                    db = DriverManager.getConnection(url, userName, userPass);

                    LOGGER.info("Moving streams .. ");
                    MZKMigrationParts.STREAMS.doMigrationPart(db);
                    LOGGER.info("Moving objects ..");
                    MZKMigrationParts.OBJECTS.doMigrationPart(db);

                } finally {
                    db.close();
                }
            }

            @Override
            public String desc() {
                StringBuilder builder = new StringBuilder();
                builder.append("Migrace z MZK - legacy_fs -> akubra_fs, zmena reference streamu IMG_FULL, IMG_PREVIEW, IMG_THUMB a pridani sbirky").append('\n');
                builder.append("Nutne promenne pro migraci: ").append('\n');
                builder.append("\tfedora3.connectionURL").append(" - db konekce do fedory").append('\n');
                builder.append("\tfedora3.connectionUser").append(" - db uzivatel").append('\n');
                builder.append("\tfedora3.connectionPass").append(" - db pass").append('\n');
                builder.append("\t\tPoznamka: Cesty v databazi musi platne !").append('\n');

                builder.append("\takubrafs.streams.target").append(" - adresar ciloveho akubra_fs").append('\n');
                builder.append("\takubrafs.objects.target").append(" - adresar ciloveho akubra_fs").append('\n');
                builder.append("\trelsext.collection").append(" - pid sbirky").append('\n');
                builder.append("\timgfull.replace").append(" - adresa pro stream IMG_FULL").append('\n');
                builder.append("\timgpreview.replace").append(" - adresa pro stream IMG_PREVIEW ").append('\n');
                builder.append("\timgthumb.replace").append(" - adresa pro stream IMG_THUMB");

                return builder.toString();
            }
        };


        public abstract  void doCommand() throws Exception;

        public abstract String desc();
    }

}
