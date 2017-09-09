
import { requireNativeComponent, View } from 'react-native';
import { PropTypes } from 'react';

const pdfPaging = {
          name: 'PdfPaging',
          propTypes: {
               path: PropTypes.string,
               currentPage: PropTypes.number,
               getPageCount: PropTypes.func,
               getCurrentPage: PropTypes.func,
                ...View.propTypes
          }
}

const PdfPaging = requireNativeComponent('PdfPagingManager', pdfPaging);


export default PdfPaging;
