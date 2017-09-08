
import { requireNativeComponent, View } from 'react-native';
import { PropTypes } from 'react';

const myText = {
          name: 'MyText',
          propTypes: {
               text: PropTypes.string,
                ...View.propTypes
          }
}

const MyText = requireNativeComponent('PdfPagingManager', myText);


export default MyText;
