
import { requireNativeComponent, View } from 'react-native';
import { PropTypes } from 'react';

const pdfPaging = {
          name: 'PdfPaging',
          propTypes: {
               path: PropTypes.string,
                ...View.propTypes
          }
}

const PdfPaging = requireNativeComponent('PdfPagingManager', pdfPaging);


export default PdfPaging;
