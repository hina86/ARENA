package cleanup;

import com.opencsv.bean.BeanField;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

public class CustomMappingStrategy<T> extends ColumnPositionMappingStrategy<T> {
    private boolean useHeader=true;

    public CustomMappingStrategy(){
    }

    public CustomMappingStrategy(boolean useHeader) {
        this.useHeader = useHeader;
    }

    @Override
    public String[] generateHeader(T bean) throws CsvRequiredFieldEmptyException {
        final int numColumns = FieldUtils.getAllFields(bean.getClass()).length;
        super.setColumnMapping(new String[numColumns]);

        if (numColumns == -1) {
            return super.generateHeader(bean);
        }

        String[] header = new String[numColumns];

        if(!useHeader){
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        BeanField<T, Integer> beanField;
        for (int i = 0; i < numColumns; i++){
            beanField = findField(i);
            String columnHeaderName = extractHeaderName(beanField);
            header[i] = columnHeaderName;
        }

        return header;
    }

    private String extractHeaderName(final BeanField<T, Integer> beanField){
        if (beanField == null || beanField.getField() == null || beanField.getField().getDeclaredAnnotationsByType(CsvBindByName.class).length == 0){
            return StringUtils.EMPTY;
        }

        //return value of CsvBindByName annotation
        final CsvBindByName bindByNameAnnotation = beanField.getField().getDeclaredAnnotationsByType(CsvBindByName.class)[0];
        return bindByNameAnnotation.column();
    }

}