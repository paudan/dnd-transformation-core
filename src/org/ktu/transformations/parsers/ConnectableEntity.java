package org.ktu.transformations.parsers;

import java.util.Collections;
import java.util.Set;

/**
 * Class representing a mapping element in source part or target part 
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems 
 * Design Technologies, Kaunas University of Technology, 2014-2015
 */
public class ConnectableEntity {
    
    private String name, processedName, name2, name3;
    private String printableName;
    private String typeName;
    private Object objectRef, typeRef, profileRef, representedStRef;
    private boolean isStereotype;
    private boolean draggedElement;
    private Class<?> baseClass;
    private Set<Class> baseClasses;

    public ConnectableEntity(Object objectRef, Object typeRef) {
        this.objectRef = objectRef;
        this.typeRef = typeRef;
        this.profileRef = null;
        this.representedStRef = null;
    }

    /**
     * Get actual name of the mapping element 
     * @return {@link String} representing name
     */
    public String getName() {
        return name;
    }

    /**
     * Set actual name of the mapping element
     * @param name {@link String} representing name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get printable name of the mapping element 
     * @return {@link String} representing name
     */
    public String getPrintableName() {
        return printableName;
    }

    /**
     * Set printable name of the mapping element
     * @param printableName {@link String} representing name
     */
    public void setPrintableName(String printableName) {
        this.printableName = printableName;
    }

    /**
     * Checks if the mapping element is also marked as "dragged" element (i.e., it corresponds to 
     * an element which would be selected as "selected" or "dragged" during drag and drop action or selection/call actions)
     * @return {@code true} if the element is marked as "dragged" element; {@code false} otherwise
     */
    public boolean isDraggedElement() {
        return draggedElement;
    }

    /**
     * Set the mapping element as "dragged" element
     * @param draggedElement Indicates if this object represents a mapping element with "dragged" mark
     */
    public void setDraggedElement(boolean draggedElement) {
        this.draggedElement = draggedElement;
    }
    
    /**
     * Return the UML ConnectableElement associated with this object
     * @return Object representing the UML element associated with this element
     */
    public Object getConnectableObject() {
        return objectRef;
    }
    
    @Override
    public boolean equals(Object o1) {
        if (o1 instanceof ConnectorEntity)
            return objectRef.equals(((ConnectableEntity)o1).objectRef);
        return (this == o1);
    }
    
    @Override
    public int hashCode() {
        return objectRef.hashCode();
    }

    /**
     * Return UML Profile associated with this mapping element, if it also represents UML Stereotype
     * @return An Object representing UML Profile
     */
    public Object getProfile() {
        return profileRef;
    }

    /**
     * Set UML Profile associated with this mapping element
     * @param profileRef An Object representing UML Profile
     */
    public void setProfile(Object profileRef) {
        this.profileRef = profileRef;
    }

    /**
     * Get UML Type of this mapping element
     * @return Object representing UML type
     */
    public Object getType() {
        return typeRef;
    }

    /**
     * Checks if this object represents a UML stereotype
     * @return {@code true} if this object represents a UML Stereotype; {@code false} otherwise
     */
    public boolean isStereotype() {
        return isStereotype;
    }

    /**
     * Set this mapping object to represent a UML stereotype
     * @param isStereotype {@code true} if this object represents a UML Stereotype; {@code false} otherwise
     */
    public void setIsStereotype(boolean isStereotype) {
        this.isStereotype = isStereotype;
    }

    public String getName3() {
        return name3;
    }

    public void setName3(String name3) {
        this.name3 = name3;
    }

    /**
     * Get name, obtained after preprocessing name of represented UML ConnectableElement, of the mapping element 
     * @return {@link String} representing name
     */
    public String getProcessedName() {
        return processedName;
    }

    /**
     * Set the name, obtained after preprocessing name of represented UML ConnectableElement, of the mapping element
     * @param processedName {@link String} representing name
     */
    public void setProcessedName(String processedName) {
        this.processedName = processedName;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    /**
     * Get base class of the UML element, represented by this mapping entity 
     * @return The base class
     */
    public Class<?> getBaseClass() {
        return baseClass;
    }

    /**
     * Set base class of the UML element, represented by this mapping entity 
     * @param baseClass  The base class
     */
    public void setBaseClass(Class<?> baseClass) {
        this.baseClass = baseClass;
    }

    /**
     * Get the name of UML Connectable Element type, represented by this mapping entity
     * @return {@link String} representing type name
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Set the name of the UML Connectable Element type, represented by this mapping entity
     * @param typeName  Type name 
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * Get base classes of the UML element represented by this mapping entity. If this element is a stereotype, more than one class may be defined for it
     * @return An immutable {@link Set} of base classes
     */
    public Set<Class> getBaseClasses() {
        return Collections.unmodifiableSet(baseClasses);
    }

    /**
     * Set base classes of the UML element represented by this mapping entity. 
     * If this element is a stereotype, more than one class may be defined for it
     * @param baseClasses The {@link Set} of base classes
     */
    public void setBaseClasses(Set<Class> baseClasses) {
        this.baseClasses = baseClasses;
    }

    /**
     * Get the stereotype object represented by this mapping entity
     * @return {@link Object} representing UML Stereotype
     */
    public Object getRepresentedStereotype() {
        return representedStRef;
    }

    /**
     * Set the stereotype object represented by this mapping entity
     * @param stereotypeRef Object, representing UML Stereotype
     */
    public void setRepresentedStereotype(Object stereotypeRef) {
        this.representedStRef = stereotypeRef;
    }
    
}
