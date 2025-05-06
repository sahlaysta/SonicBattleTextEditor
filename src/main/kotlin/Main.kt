import sahlaysta.sbte4.gui.GUI

fun main(args: Array<String>) {
    GUI.start(if (args.size == 1) args[0] else null)
}