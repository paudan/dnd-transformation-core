package org.ktu.transformations.parsers;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * The structure, representing the path to particular property in element's property tree
 * @author Paulius Danenas ({@literal danpaulius@gmail.com}), Center of Information Systems Design Technologies, 
 * Kaunas University of Technology, 2015
 */
public class PropertyStack {

    private Stack<ConnectableEntity> stack;
    private Stack<Object> stackRef;

    public PropertyStack() {
        stack = new Stack<>();
        stackRef = new Stack<>();
    }

    /**
     * Check if PropertyStack object is empty
     * @return {@code true} if this object is empty; {@code false} otherwise
     */
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    /**
     * Return the number of mapping elements in this property stack (i.e., the depth of this stack)
     * @return The number of elements in this property stack
     */
    public int size() {
        return stack.size();
    }

    /**
     * Check if this object contains the given {@link ConnectableEntity}
     * @param e The element to search for
     * @return {@code true} if this property stack contains the given mapping element of type {@link ConnectableEntity}; {@code false} otherwise
     */
    public boolean contains(ConnectableEntity e) {
        return stack.contains(e);
    }
    
    /**
     * Check if this object contains the actual UML mapping element
     * @param ref   The UML element (usually ConnectableElement) to search for
     * @return      {@code true} if this object contains the given UML mapping element; {@code false} otherwise
     */
    public boolean containsRef(Object ref) {
        return stackRef.contains(ref);
    }

    /**
     * Add a property element to the stack
     * @param e The element which must be added
     */
    public void add(ConnectableEntity e) {
        stack.add(e);
        stackRef.add(e.getConnectableObject());
    }

    /**
     * Appends all of the elements in the specified Collection to the end of this PropertyStack,
     * in the order that they are returned by the specified Collection's Iterator.
     * @param c The {@link Collection} of elements to be added
     */
    public void addAll(Collection<? extends ConnectableEntity> c) {
        for (ConnectableEntity o: c) {
            stack.add(o);
            stackRef.add(o.getConnectableObject());
        }
    }

    /**
     * Appends all of the contents of the specified PropertyStack to the end of this PropertyStack,
     * in the order that they are returned by the specified Collection's Iterator.
     * @param c The {@link Collection} of elements to be added
     */
    public void addAll(PropertyStack c) {
        for (ConnectableEntity o: c.stack) {
            stack.add(o);
            stackRef.add(o.getConnectableObject());
        }
    }
    
    /**
     * Inserts all of the elements in the specified PropertyStack into this PropertyStack at the specified position
     * @param index     index at which to insert the first element from the specified PropertyStack 
     * @param c         elements to be inserted into this PropertyStack
     */
    public void addAll(int index, PropertyStack c) {
        stack.addAll(index, c.stack);
        Stack<Object> refs = new Stack<>();
        for (ConnectableEntity o: c.stack) 
            refs.add(o.getConnectableObject());
        stackRef.addAll(refs);
    }
    
    /**
     * Inserts all of the elements in the specified {@link Collection} into this PropertyStack at the specified position
     * @param index     index at which to insert the first element from the specified collection 
     * @param c         elements to be inserted into this PropertyStack
     */
    public void addAll(int index, Collection<? extends ConnectableEntity> c) {
        stack.addAll(index, c);
        Stack<Object> refs = new Stack<>();
        for (ConnectableEntity o: c) 
            refs.add(o.getConnectableObject());
        stackRef.addAll(refs);
    }
    
    /**
     * Returns a view of the portion of this List between fromIndex, inclusive, and toIndex, exclusive. 
     * If fromIndex and toIndex are equal, the returned List is empty. The returned List supports all of the optional List operations supported by this List.
     * @param fromIndex  The lower bound (inclusive) of the subList
     * @param toIndex    The upper bound (exclusive) of the subList
     * @return  A view of the specified range within this PropertyStack
     * @see Stack#subList(int, int) 
     */
    public List<ConnectableEntity> subList(int fromIndex, int toIndex) {
        return stack.subList(fromIndex, toIndex);
    }

    /**
     * Return the mapping element, representing the Element, containing this property
     * @return The ConnectableElement representing the mapping element
     */
    public ConnectableEntity metaElement() {
        return stack.firstElement();
    }

    /**
     * Returns the contents of the PropertyStack as unmodifiable {@link List}
     * @return  {@link List} of the ConnectableElements in the PropertyStack
     */
    public List<ConnectableEntity> asUnmodifiableList() {
        return Collections.unmodifiableList(stack);
    }
    
    /**
     * Pushes an  ConnectableEntity to the top of this PropertyStack
     * @param e {@link ConnectableEntity} object which is added to the stack
     * @return {@code e} argument
     */
    public ConnectableEntity push(ConnectableEntity e) {
        stackRef.push(e.getConnectableObject());
        return stack.push(e);
    }
    
    /**
     * Returns the element at the specified position in this PropertyStack.
     * @param index     The index of the ConnectableElement to be returned
     * @return          {@link ConnectableEntity} representing particular property at the given position of the PropertyStack
     */
    public ConnectableEntity get(int index) {
        return stack.get(index);
    }
    
    /**
     * Return the topmost property, as it is represented in the transformation pattern (i.e., property that is mapped)
     * @return The ConnectableElement representing the topmost property
     */
    public ConnectableEntity lowermostProperty() {
        return stack.lastElement();
    }

    /**
     * Return a string representing {@link PropertyStack} object
     * @return The {@link String} representation of this object
     */
    @Override
    public String toString() {
        if (this == null || stack.isEmpty())
            return "";
        StringBuilder result = new StringBuilder();
        result.append("{");
        for (ConnectableEntity key : stack)
            result.append(key.getPrintableName()).append(", ");
        result.delete(result.length() - 2, result.length()).append("}");
        return result.toString();
    }
}
