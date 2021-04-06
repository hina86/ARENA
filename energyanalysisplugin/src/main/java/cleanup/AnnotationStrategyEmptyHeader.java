package cleanup;

import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.commons.lang.ArrayUtils;

public class AnnotationStrategyEmptyHeader extends HeaderColumnNameTranslateMappingStrategy {
    public AnnotationStrategyEmptyHeader(Class clazz) {
        setType(clazz);
    }

    @Override
    public String[] generateHeader(Object bean) throws CsvRequiredFieldEmptyException {
        super.generateHeader(bean);
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }
}
