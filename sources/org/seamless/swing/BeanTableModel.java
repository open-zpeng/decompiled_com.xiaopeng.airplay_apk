package org.seamless.swing;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.table.AbstractTableModel;
/* loaded from: classes.dex */
public class BeanTableModel<T> extends AbstractTableModel {
    private Class<T> beanClass;
    private List<PropertyDescriptor> properties = new ArrayList();
    private List<T> rows;

    public BeanTableModel(Class<T> beanClass, Collection<T> rows) {
        this.beanClass = beanClass;
        this.rows = new ArrayList(rows);
    }

    public String getColumnName(int column) {
        return this.properties.get(column).getDisplayName();
    }

    public int getColumnCount() {
        return this.properties.size();
    }

    public int getRowCount() {
        return this.rows.size();
    }

    public Object getValueAt(int row, int column) {
        T entityInstance = this.rows.get(row);
        if (entityInstance == null) {
            return null;
        }
        PropertyDescriptor property = this.properties.get(column);
        Method readMethod = property.getReadMethod();
        try {
            Object value = readMethod.invoke(entityInstance, new Object[0]);
            return value;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void addColumn(String displayName, String propertyName) {
        try {
            PropertyDescriptor property = new PropertyDescriptor(propertyName, this.beanClass, propertyName, (String) null);
            property.setDisplayName(displayName);
            this.properties.add(property);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void resetColumns() {
        this.properties = new ArrayList();
    }

    public List<T> getRows() {
        return this.rows;
    }

    public void setRows(Collection<T> rows) {
        this.rows = new ArrayList(rows);
        fireTableDataChanged();
    }

    public void addRow(T value) {
        this.rows.add(value);
        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
    }

    public void removeRow(int row) {
        if (this.rows.size() > row && row != -1) {
            this.rows.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }

    public void setRow(int row, T entityInstance) {
        this.rows.remove(row);
        this.rows.add(row, entityInstance);
        fireTableDataChanged();
    }
}
