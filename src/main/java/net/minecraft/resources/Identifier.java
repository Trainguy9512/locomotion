//? if < 1.21.0 {
/*package net.minecraft.resources;

// Shim for <1.21.0 where Mojang mappings use ResourceLocation instead of Identifier.
// This lets shared code keep using net.minecraft.resources.Identifier.
public class Identifier extends ResourceLocation {

    public Identifier(String namespace, String path) {
        super(namespace, path);
    }

    public Identifier(String location) {
        super(location);
    }

    public static Identifier fromNamespaceAndPath(String namespace, String path) {
        return new Identifier(namespace, path);
    }

    public static Identifier withDefaultNamespace(String path) {
        return new Identifier(path);
    }
}
*///?}
