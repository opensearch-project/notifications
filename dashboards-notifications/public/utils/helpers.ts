import moment from 'moment';

export function getErrorMessage(err: any, defaultMessage: string) {
  if (err && err.message) return err.message;
  return defaultMessage;
}

export const renderTime = (time: number): string => {
  const momentTime = moment(time).local();
  if (time && momentTime.isValid()) return momentTime.format('MM/DD/YY h:mm a');
  return '-';
};
