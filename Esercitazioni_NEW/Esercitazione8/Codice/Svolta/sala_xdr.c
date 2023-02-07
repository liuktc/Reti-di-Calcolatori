/*
 * Please do not edit this file.
 * It was generated using rpcgen.
 */

#include "sala.h"

bool_t
xdr_Input (XDR *xdrs, Input *objp)
{
	register int32_t *buf;

	 if (!xdr_char (xdrs, &objp->tipo))
		 return FALSE;
	 if (!xdr_int (xdrs, &objp->fila))
		 return FALSE;
	 if (!xdr_int (xdrs, &objp->colonna))
		 return FALSE;
	return TRUE;
}

bool_t
xdr_Fila (XDR *xdrs, Fila *objp)
{
	register int32_t *buf;

	int i;
	 if (!xdr_vector (xdrs, (char *)objp->posto, LUNGHFILA,
		sizeof (char), (xdrproc_t) xdr_char))
		 return FALSE;
	return TRUE;
}

bool_t
xdr_Sala (XDR *xdrs, Sala *objp)
{
	register int32_t *buf;

	int i;
	 if (!xdr_vector (xdrs, (char *)objp->fila, NUMFILE,
		sizeof (Fila), (xdrproc_t) xdr_Fila))
		 return FALSE;
	return TRUE;
}