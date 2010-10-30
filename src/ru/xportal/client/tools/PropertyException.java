package ru.xportal.client.tools;

/**
 * Этот тип исключений генерируют методы класса Properties при неудачном
 * извлечении значения свойства. Может быть использовано для безопасной
 * установки значений по-умолчанию.
 * @author airat
 */
public class PropertyException extends RuntimeException {

    public PropertyException() {
        super("PROPERTY FAILURE");
    }

}
