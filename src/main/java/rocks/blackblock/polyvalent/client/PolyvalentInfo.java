package rocks.blackblock.polyvalent.client;

import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import rocks.blackblock.polyvalent.Polyvalent;

/**
 * Abstract Polyvalent info class
 *
 * @author   Jelle De Loecker   <jelle@elevenways.be>
 * @since    0.2.0
 */
public abstract class PolyvalentInfo {

    // The identifier of the server-side thing this is representing
    protected Identifier identifier;

    // The namespace of this item
    protected String namespace;

    /**
     * Set the identifier
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.2.0
     */
    protected void setIdentifier(String identifier) {
        this.setIdentifier(Identifier.tryParse(identifier));
    }

    /**
     * Set the identifier
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.2.0
     */
    protected void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
        this.namespace = identifier.getNamespace();
    }

    /**
     * Get the identifier
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.2.0
     */
    public Identifier getIdentifier() {
        return this.identifier;
    }

    /**
     * Get the title of the mod/namespace
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.2.0
     */
    public String getNamespaceTitle() {
        return Polyvalent.getNamespaceTitle(this.identifier);
    }

    /**
     * Get the mod name of this item
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.1
     */
    public String getModName() {

        if (Language.getInstance().hasTranslation(this.namespace)) {
            return Language.getInstance().get(this.namespace);
        }

        return this.namespace.substring(0, 1).toUpperCase() + this.namespace.substring(1);
    }

}
