package io.github.mores;

public class App {

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(App.class);

    private String[] args;

    public static void main(String[] args) throws Exception {

        new App(args).run();
    }

    public App(String[] args) {
        this.args = args;
    }

    public void run() throws Exception {
        log.info("Running");

        gnu.getopt.LongOpt[] longopts = new gnu.getopt.LongOpt[3];

        longopts[0] = new gnu.getopt.LongOpt("help", gnu.getopt.LongOpt.NO_ARGUMENT, null, 'h');
        longopts[1] = new gnu.getopt.LongOpt("file", gnu.getopt.LongOpt.REQUIRED_ARGUMENT, null, 'f');
        longopts[2] = new gnu.getopt.LongOpt("module", gnu.getopt.LongOpt.REQUIRED_ARGUMENT, null, 'm');

        int c;
        java.io.File file = null;
        String module = null;

        gnu.getopt.Getopt g = new gnu.getopt.Getopt("stl2openSCAD", args, "hf:m:", longopts);
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'f':
                    file = new java.io.File(g.getOptarg());
                    break;
                case 'm':
                    module = g.getOptarg();
                    break;
                case 'h':
                default:
                    usage();
                    break;
            }
        }

        if (file == null) {
            usage();
        }

        java.io.FileInputStream fileInputStream = new java.io.FileInputStream(file);

        StlConverter stlConverter = new StlConverter(fileInputStream);
        stlConverter.convert();
        log.debug("Data:  " + stlConverter.getScad());

        String name = file.getName();
        int dot = name.lastIndexOf('.');

        String newName = (dot == -1) ? name + ".scad" : name.substring(0, dot) + ".scad";

        java.io.File newFile = new java.io.File(file.getParent(), newName);
        java.io.PrintWriter pw = new java.io.PrintWriter(newFile);
        pw.println("module " + module + "(scale) { translate([0,0,0]) rotate([0,0,0]) polyhedron(");
        pw.println(stlConverter.getScad());
        pw.println(");}");
        pw.close();
    }

    public static void usage() {
        System.out.println("usage: stl2openSCAD [ -hf ]\n" + "   [ -f ] [ --file=<file> ] \n" + "   [ -h ] [ --help ]\n"
                + "   [ -m ] [ --module=<module> ] \n");
        System.exit(1);
    }
}
