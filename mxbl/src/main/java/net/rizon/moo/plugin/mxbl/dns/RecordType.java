package net.rizon.moo.plugin.mxbl.dns;

/**
 * Enumerator for NS Record types.
 * <p>
 * @author Orillion <orillion@rizon.net>
 */
public enum RecordType
{
	A("A"),
	AAAA("AAAA"),
	AFSDB("AFSDB"),
	APL("APL"),
	CAA("CAA"),
	CDNSKEY("CDNSKEY"),
	CDS("CDS"),
	CERT("CERT"),
	CNAME("CNAME"),
	DHCID("DHCID"),
	DLV("DLV"),
	DNAME("DNAME"),
	DNSKEY("DNSKEY"),
	DS("DS"),
	HIP("HIP"),
	IPSECKEY("IPSECKEY"),
	KEY("KEY"),
	KX("KX"),
	LOC("LOC"),
	MX("MX"),
	NAPTR("NAPTR"),
	NS("NS"),
	NSEC("NSEC"),
	NSEC3("NSEC3"),
	NSEC3PARAM("NSEC3PARAM"),
	PTR("PTR"),
	RRSIG("RRSIG"),
	RP("RP"),
	SIG("SIG"),
	SOA("SOA"),
	SRV("SRV"),
	TA("TA"),
	TKEY("TKEY"),
	TLSA("TLSA"),
	TSIG("TSIG"),
	TXT("TXT"),
	AXFR("AXFR"),
	IXFR("IXFR"),
	OPT("OPT"),
	ALL("*");

	private final String name;

	/**
	 * Creates a new {@link RecordType} enumerator.
	 * <p>
	 * @param s Name of the MX record.
	 */
	private RecordType(String s)
	{
		this.name = s;
	}

	/**
	 * Returns the name of this {@link RecordType} in {@link String} format.
	 * <p>
	 * @return {@link RecordType} name.
	 */
	@Override
	public String toString()
	{
		return this.name;
	}

}
