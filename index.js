
import { requireNativeComponent, View } from 'react-native';
import { PropTypes } from 'react';

const pdfPaging = {
          name: 'PdfPaging',
          propTypes: {
               text: PropTypes.string,
                ...View.propTypes
          }
}

const PdfPaging = requireNativeComponent('PdfPagingManager', pdfPaging);


export default PdfPaging;
